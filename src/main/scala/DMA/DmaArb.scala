package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}


case class DmaArbiter(c:DmaCfg) extends Component{
  val io = new Bundle{
    val getNextCmd = in Bool()
    val nodeList = Vec(slave(DmaNodeInf(c,NodeType.fullVersion)), c.slaveNode )
    val finalNode = master(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
  }

  //set default value
  if (c.slaveNode == 1) {
    io.finalNode.connect(io.nodeList(0),0)
  } else {
    io.finalNode.connect(io.nodeList(0),0)
    for (node <- io.nodeList.slice(1 , c.slaveNode)) {
      node.cmd.cmdStream.ready := False
      node.wrChannel.wrStream.ready := False
    }
  }

  for (i <- 0 until  c.slaveNode) {
    when(i === io.finalNode.rsp.rspStream.id) {
      io.nodeList(i).rsp.rspStream.valid <> io.finalNode.rsp.rspStream.valid
      io.nodeList(i).rsp.rspStream.ready <> io.finalNode.rsp.rspStream.ready
      io.nodeList(i).rsp.rspStream.rspCode <> io.finalNode.rsp.rspStream.rspCode
    } otherwise {
      io.nodeList(i).rsp.rspStream.valid := False
      io.nodeList(i).rsp.rspStream.rspCode := 0
      io.finalNode.rsp.rspStream.ready := False
    }
  }

  for (i <- 0 until c.slaveNode) {
    when(i === io.finalNode.rdChannel.rdStream.fragment(io.finalNode.rdChannel.rdStream.fragment.getWidth-1 downto 32)) {
      io.nodeList(i).rdChannel.rdStream.valid <> io.finalNode.rdChannel.rdStream.valid
      io.nodeList(i).rdChannel.rdStream.last <> io.finalNode.rdChannel.rdStream.last
      io.nodeList(i).rdChannel.rdStream.ready <> io.finalNode.rdChannel.rdStream.ready
      io.nodeList(i).rdChannel.rdStream.fragment := io.finalNode.rdChannel.rdStream.fragment(31 downto 0)
    } otherwise {
      io.nodeList(i).rdChannel.rdStream.fragment := 0
      io.nodeList(i).rdChannel.rdStream.last := False
      io.nodeList(i).rdChannel.rdStream.valid := False
      io.finalNode.rdChannel.rdStream.ready := False
    }
  }



  val maskLocked  = Vec(Reg(Bits(c.slaveNode bits)) init(BigInt(1) << (c.slaveNode - 1)), BigInt(2).pow(c.priWidth).toInt)
  val maskProposal    = Bits(c.slaveNode bits)
  // 第一维度优先级，第二维度当前优先级下各slave的请求bitmap
  val priList:IndexedSeq[Bits] = for (i <- 0 until  BigInt(2).pow(c.priWidth).toInt) yield {
    (for(node <- io.nodeList) yield ((node.cmd.cmdStream.pri === U(i,c.priWidth bits)) && node.cmd.cmdStream.valid)).asBits()
  }

  // 每个优先级是否有请求
  val priListBitmap = for (i <- 0 until BigInt(2).pow(c.priWidth).toInt) yield priList(i).asUInt =/= 0

  val (d1Exist,highVldPri) = priListBitmap.sFindFirst(_ === True)
  val selectPri = d1Exist ? highVldPri|0
  val selReq = priList(selectPri)


  maskProposal := OHMasking.roundRobin(selReq,maskLocked(selectPri)(maskLocked(selectPri).high - 1 downto 0) ## maskLocked(selectPri).msb)

  when(io.getNextCmd) {
    maskLocked(selectPri) := maskProposal
  }

  for ((input,requestRouted) <- (io.nodeList,maskProposal.asBools).zipped) {
    when(requestRouted) {
      input.connect(io.finalNode,OHToUInt(maskProposal))
    }
  }



}

case class DmaArbCtrl() extends Component {
  val io = new Bundle {
    val dmaEnable = in Bool()
    val dmaReady = in Bool()
    val cmdFire = in Bool()
    val getNextCmd = out Bool()
    val lastDataFired = in Bool()
  }

  val ctrlST = new StateMachine {
    val IDLE:State = new State with EntryPoint {
      whenIsActive {
        when(io.dmaEnable && io.dmaReady) {
          goto(WaitFire)
        }
      }
    }
    val WaitFire:State = new State {
      whenIsActive{
        when(io.cmdFire) {
          goto(
            WaitDmaDone
          )
        }
      }
    }
    val WaitDmaDone:State = new State {
      whenIsActive {
        when(io.lastDataFired) {
          goto(IDLE)
        }
      }
    }
  }

  io.getNextCmd := ctrlST.isActive(ctrlST.IDLE) && io.dmaEnable && io.dmaReady
}


case class DmaArbWrapper(c:DmaCfg) extends Component {
  val io = new Bundle{
    val dmaEnable = in Bool()
    val dmaReady = in Bool()
    val nodeList = Vec(slave(DmaNodeInf(c,NodeType.fullVersion)), c.slaveNode )
    val finalNode = master(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
  }
  val arbiter = DmaArbiter(c)
  val ctrl = DmaArbCtrl()
  val wrOp = RegNextWhen(io.finalNode.cmd.cmdStream.wrOp,io.finalNode.cmd.cmdStream.fire)
  arbiter.io.nodeList <> io.nodeList
  arbiter.io.finalNode <> io.finalNode
  arbiter.io.getNextCmd <> ctrl.io.getNextCmd
  ctrl.io.dmaReady := io.dmaReady
  ctrl.io.dmaEnable := io.dmaEnable
  ctrl.io.cmdFire := io.finalNode.cmd.cmdStream.fire


  ctrl.io.lastDataFired := wrOp ? (io.finalNode.wrChannel.wrStream.last && io.finalNode.wrChannel.wrStream.fire) | True

}