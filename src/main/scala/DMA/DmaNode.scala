package DMA
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

object NodeType extends Enumeration{
  val fullVersion = 0
  val writeOnly = 1
  val readOnly = 2
}

case class DmaNodeInf(c:DmaCfg, nodeType:Int, withSlaveId:Boolean = false) extends Bundle with IMasterSlave {
  val cmd = CmdChannel(c,withSlaveId)
  val wrChannel = if(nodeType != NodeType.readOnly) WrChannel(c,withSlaveId) else null
  val rdChannel = if(nodeType != NodeType.writeOnly) RdChannel(c,withSlaveId) else null

  override def asMaster(): Unit = {
    master(cmd,wrChannel,rdChannel)
  }

  def connect(a:DmaNodeInf,slaveId:UInt = 0) = {
    if (withSlaveId == a.withSlaveId) {
      cmd <> a.cmd
      wrChannel <> a.wrChannel
      rdChannel <> a.rdChannel
    } else if (withSlaveId == true) {
      for ((name, element)  <- a.cmd.elements) {
        val other = cmd.find(name)
        if (other != null) {
          element <> other
        }
      }

      for ((name, element)  <- a.wrChannel.wrStream.elements) {
        val other = wrChannel.wrStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }

      for ((name, element)  <- a.rdChannel.rdStream.elements) {
        val other = rdChannel.rdStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }
      if (cmd.id != null) {
        cmd.id := slaveId.asBits.resize(cmd.id.getWidth)
      }
      wrChannel.wrStream.fragment := slaveId.asBits.resize(cmd.id.getWidth) ## a.wrChannel.wrStream.fragment
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last
      a.rdChannel.rdStream.fragment := rdChannel.rdStream.fragment(c.dataWidth-1 downto 0)
      a.rdChannel.rdStream.last <> rdChannel.rdStream.last
    } else {
      for ((name, element)  <- a.cmd.elements) {
        val other = cmd.find(name)
        if (other != null) {
          element <> other
        }
      }
      for ((name, element)  <- a.wrChannel.wrStream.elements) {
        val other = wrChannel.wrStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }

      for ((name, element)  <- a.rdChannel.rdStream.elements) {
        val other = rdChannel.rdStream.find(name)
        if (other != null  && !name.contains("payload")) {
          element <> other
        }
      }
      if (a.cmd.id != null) {
        a.cmd.id := slaveId.asBits.resize(a.cmd.id.getWidth)
      }

      wrChannel.wrStream.fragment <> a.wrChannel.wrStream.fragment(c.dataWidth-1 downto 0)
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last
      a.rdChannel.rdStream.fragment <> slaveId.asBits.resize(a.cmd.id.getWidth) ## rdChannel.rdStream.fragment
      a.rdChannel.rdStream.last <> rdChannel.rdStream.last
    }
  }
}

case class DmaArbiter(c:DmaCfg) extends Component{
  val io = new Bundle{
    val getNextCmd = in Bool()
    val nodeList = Vec(master(DmaNodeInf(c,NodeType.fullVersion)), c.slaveNode )
    val finalNode = slave(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
  }

  //set default value
  if (c.slaveNode == 1) {
    io.finalNode.connect(io.nodeList(0),0)
  } else {
    io.finalNode.connect(io.nodeList(0),0)
    for (node <- io.nodeList.slice(1 , c.slaveNode)) {
      node.cmd.rspStream.valid := False
      node.cmd.rspStream.payload := 0
      node.cmd.reqReady := False
      node.wrChannel.wrStream.ready := False
      node.rdChannel.rdStream.fragment := 0
      node.rdChannel.rdStream.last := False
      node.rdChannel.rdStream.valid := False

    }
  }


    val maskLocked  = Vec(Reg(Bits(c.slaveNode bits)) init(BigInt(1) << (c.slaveNode - 1)), BigInt(2).pow(c.priWidth).toInt)
    val maskProposal    = Bits(c.slaveNode bits)
    // 第一维度优先级，第二维度当前优先级下各slave的请求bitmap
    val priList:IndexedSeq[Bits] = for (i <- 0 until  BigInt(2).pow(c.priWidth).toInt) yield {
      (for(node <- io.nodeList) yield ((node.cmd.pri === U(i,c.priWidth bits)) && node.cmd.reqVld)).asBits()
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

case class NodeExceptionDetecter(c:DmaCfg) extends Component{

  val io = new Bundle{
    val node = master(DmaNodeInf(c,NodeType.fullVersion))
    val nodeOut = slave(DmaNodeInf(c,NodeType.fullVersion))
  }


  val cmdFire = io.node.cmd.reqVld && io.node.cmd.reqReady
  //exception detect
  val dirLatch = RegNextWhen(io.node.cmd.wrOp,cmdFire,init = False)
  val transLenLatch = RegNextWhen(io.node.cmd.transLen,cmdFire)
  val transCnt = Reg(UInt(c.lenWidth bits)) init 0
  val goldenLastFlag = False
  val forceLast = False
  val forceVld = False
  val forceReady = False
  val forceRsp = False
  val forceCode = Bits(c.rspWidth bits)
  val lastUnmatchErr = False
  val dir = cmdFire ? io.node.cmd.wrOp | dirLatch
  val watchDog = UInt(10 bits)

  when((dir && io.nodeOut.wrChannel.wrStream.last) || (!dir && io.node.rdChannel.rdStream.last) || io.node.cmd.rspStream.valid) {
    transCnt := 0
  } elsewhen(cmdFire) {
    when(io.node.cmd.wrOp) {
      when(io.node.wrChannel.wrStream.fire) {
        transCnt := 1
      } otherwise {
        transCnt := 0
      }
    } otherwise {
      when(io.node.rdChannel.rdStream.fire) {
        transCnt := 1
      } otherwise {
        transCnt := 0
      }
    }
  } otherwise {
    when(io.node.cmd.wrOp) {
      when(io.node.wrChannel.wrStream.fire) {
        transCnt := transCnt + 1
      }
    } otherwise {
      when(io.node.rdChannel.rdStream.fire) {
        transCnt := transCnt + 1
      }
    }
  }

  //gen golden last flag accord translen
  when(cmdFire) {
    when(io.node.cmd.transLen === 1){
      goldenLastFlag := True
    } otherwise {
      goldenLastFlag := False
    }
  } otherwise {
    goldenLastFlag := transCnt === (transLenLatch - 1)
  }

  when(dir) {
    // last flag dont match
    when((goldenLastFlag =/= io.node.wrChannel.wrStream.last) && io.node.wrChannel.wrStream.valid) {
      lastUnmatchErr := True
      assert(False, "wr channel last flag miss match\n" + "goldenLastFlag is" + goldenLastFlag)
      when(goldenLastFlag) {
        // node stream lost last flag
        forceLast := True
      }
    }
  }

  for ((name,element) <- io.node.elements) {
    val target =io.nodeOut.find(name)
    if (!name.contains("rspStream")) {
      target <> element
    }
  }

  io.nodeOut.cmd.rspStream.ready <> io.node.cmd.rspStream.ready
  io.node.cmd.rspStream.valid := forceRsp | io.nodeOut.cmd.rspStream.valid
  io.node.cmd.rspStream.payload := forceRsp ? forceCode.asUInt | io.nodeOut.cmd.rspStream.payload

  io.nodeOut.wrChannel.wrStream.last := forceLast | io.node.wrChannel.wrStream.last
  io.nodeOut.wrChannel.wrStream.payload <> io.node.wrChannel.wrStream.payload
  io.nodeOut.wrChannel.wrStream.ready <> io.node.wrChannel.wrStream.ready
  io.nodeOut.wrChannel.wrStream.valid := forceVld | io.node.wrChannel.wrStream.valid

  io.nodeOut.rdChannel.rdStream.last <> io.node.rdChannel.rdStream.last
  io.nodeOut.rdChannel.rdStream.payload <> io.node.rdChannel.rdStream.payload
  io.node.rdChannel.rdStream.ready := forceReady | io.nodeOut.rdChannel.rdStream.ready
  io.nodeOut.rdChannel.rdStream.valid <>  io.node.rdChannel.rdStream.valid


  // detect long time no wr valid
  when(dir) {
    when(transCnt =/= 0) {
      when(io.node.wrChannel.wrStream.valid) {
        watchDog := 0
      } otherwise {
        watchDog := watchDog + 1
      }
    } otherwise {
      watchDog := 0
    }
  } otherwise{
    when(transCnt =/= 0) {
      when(io.node.rdChannel.rdStream.ready) {
        watchDog := 0
      } otherwise {
        watchDog := watchDog + 1
      }
    } otherwise {
      watchDog := 0
    }
  }

  val watchDogToFlag = RegInit(False)
  when(transCnt === 0) {
    watchDogToFlag := False
  } elsewhen(watchDog === 0x3ff) {
    watchDogToFlag := True
  }

  when(watchDogToFlag) {
    assert(False,"watch dog time out")
    when(dir) {
      forceVld := True
    } otherwise {
      forceReady := True
    }
  }

  when(watchDogToFlag.rise()) {
    forceRsp := True
    forceCode := 1<<(c.rspWidth-1)
  } elsewhen(lastUnmatchErr.rise()) {
    forceRsp := True
    forceCode := 1<<(c.rspWidth-2)
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
    val nodeList = Vec(master(DmaNodeInf(c,NodeType.fullVersion)), c.slaveNode )
    val finalNode = slave(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
  }
  val arbiter = DmaArbiter(c)
  val ctrl = DmaArbCtrl()
  arbiter.io.nodeList <> io.nodeList
  arbiter.io.finalNode <> io.finalNode
  arbiter.io.getNextCmd <> ctrl.io.getNextCmd
  ctrl.io.dmaReady := io.dmaReady
  ctrl.io.dmaEnable := io.dmaEnable
  ctrl.io.cmdFire := io.finalNode.cmd.reqVld && io.finalNode.cmd.reqReady
  ctrl.io.lastDataFired := (io.finalNode.wrChannel.wrStream.last && io.finalNode.wrChannel.wrStream.fire) || (io.finalNode.rdChannel.rdStream.last && io.finalNode.rdChannel.rdStream.fire)

}