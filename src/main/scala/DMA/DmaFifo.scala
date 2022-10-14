package DMA

import spinal.core._
import spinal.lib._
case class DmaFifo(c:DmaCfg,CmdDepth:Int,DataDepth:Int,withSlaveId:Boolean) extends Component {
  val io = new Bundle {
    val node = slave(DmaNodeInf(c,NodeType.fullVersion,withSlaveId))
    val nodeOut = master(DmaNodeInf(c,NodeType.fullVersion,withSlaveId))
  }

  val cmdFifo = new StreamFifoLowLatency(io.node.cmd.cmdStream.payload,CmdDepth)
  val rspFifo = new StreamFifoLowLatency(io.node.rsp.rspStream.payload,CmdDepth)
  val wrFifo = new StreamFifoLowLatency(io.node.wrChannel.wrStream.payload,DataDepth)
  val rdFifo = new StreamFifoLowLatency(io.node.rdChannel.rdStream.payload,DataDepth)

  cmdFifo.io.push <> io.node.cmd.cmdStream
  cmdFifo.io.pop <> io.nodeOut.cmd.cmdStream

  rspFifo.io.push <> io.nodeOut.rsp.rspStream
  rspFifo.io.pop <> io.node.rsp.rspStream

  rdFifo.io.push <> io.nodeOut.rdChannel.rdStream
  rdFifo.io.pop <> io.node.rdChannel.rdStream

  wrFifo.io.push <> io.node.wrChannel.wrStream
  wrFifo.io.pop <> io.nodeOut.wrChannel.wrStream

}
