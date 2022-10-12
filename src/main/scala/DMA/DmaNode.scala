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
  val rsp = RspChannel(c,withSlaveId)
  val wrChannel = if(nodeType != NodeType.readOnly) WrChannel(c,withSlaveId) else null
  val rdChannel = if(nodeType != NodeType.writeOnly) RdChannel(c,withSlaveId) else null

  override def asMaster(): Unit = {
    master(cmd,wrChannel)
    slave(rsp,rdChannel)
  }

  def connect(a:DmaNodeInf,slaveId:UInt = 0) = {
    if (withSlaveId == a.withSlaveId) {
      cmd <> a.cmd
      wrChannel <> a.wrChannel
    } else if (withSlaveId == true) {
      for ((name, element)  <- a.cmd.cmdStream.payload.elements) {
        val other = cmd.cmdStream.payload.find(name)
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


      if (cmd.cmdStream.id != null) {
        cmd.cmdStream.id := slaveId.asBits.resize(cmd.cmdStream.id.getWidth)
      }

      cmd.cmdStream.valid <> a.cmd.cmdStream.valid
      cmd.cmdStream.ready <> a.cmd.cmdStream.ready
      wrChannel.wrStream.fragment := slaveId.asBits.resize(cmd.cmdStream.id.getWidth) ## a.wrChannel.wrStream.fragment
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last

    } else {
      for ((name, element)  <- a.cmd.cmdStream.payload.elements) {
        val other = cmd.cmdStream.payload.find(name)
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


      if (a.cmd.cmdStream.id != null) {
        a.cmd.cmdStream.id := slaveId.asBits.resize(a.cmd.cmdStream.id.getWidth)
      }

      cmd.cmdStream.valid <> a.cmd.cmdStream.valid
      cmd.cmdStream.ready <> a.cmd.cmdStream.ready
      wrChannel.wrStream.fragment <> a.wrChannel.wrStream.fragment(c.dataWidth-1 downto 0)
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last
    }
  }
}

case class NodeRdExceptionDetecter(c:DmaCfg,withSlaveId:Boolean = true) extends Component{
  val io = new Bundle{
    val timeOutThreshold = in (UInt(10 bits))
    val node = master(RdChannel(c,withSlaveId))
    val nodeOut = slave(RdChannel(c,withSlaveId))
  }

  val watchDog = UInt(10 bits)
  val timeout = watchDog === io.timeOutThreshold
  when(io.node.rdStream.fire) {
    watchDog := 0
  } elsewhen(io.node.rdStream.valid) {
    watchDog := watchDog + 1
  }

  io.node.rdStream.valid <> io.nodeOut.rdStream.valid
  io.node.rdStream.ready := io.nodeOut.rdStream.ready | timeout
  io.node.rdStream.payload <>  io.nodeOut.rdStream.payload
}


