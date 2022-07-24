package DMA
import spinal.core._
import spinal.lib._

object NodeType extends Enumeration{
  val fullVersion = 0
  val writeOnly = 1
  val readOnly = 2
}

case class DmaNodeInf(c:DmaCfg, nodeType:Int) extends Bundle with IMasterSlave {
  val cmd = CmdChannel(c)
  val wrChannel = if(nodeType != NodeType.readOnly) WrChannel(c) else null
  val rdChannel = if(nodeType != NodeType.writeOnly) RdChannel(c) else null

  override def asMaster(): Unit = {
    master(cmd,wrChannel,rdChannel)
  }
}

case class DmaArbiter(c:DmaCfg) extends Component{
  val io = new Bundle{
    val getNextCmd = in Bool()
    val nodeList = Vec(master(DmaNodeInf(c,NodeType.fullVersion)), c.slaveNode )
    val finalNode = slave(DmaNodeInf(c,NodeType.fullVersion))
  }

  //set default value
  if (c.slaveNode == 1) {
    io.finalNode <> io.nodeList(0)
  } else {
    io.finalNode <> io.nodeList(0)
    for (node <- io.nodeList.slice(1 , c.slaveNode)) {
      node.cmd.rspVld := False
      node.cmd.rspCode := 0
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
          input <> io.finalNode
        }
    }


}