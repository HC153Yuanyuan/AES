package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config, AhbLite3Master}
import spinal.lib.com.spi.Apb3SpiMasterCtrl
import spinal.lib.fsm._
case class DmaCdc(c:DmaCfg,Depth:Int,nodeDomain:ClockDomain,busDomain:ClockDomain) extends Component {
  val io = new Bundle{
    val node = slave(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
    val nodeOut = master(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
  }

  val cmdFifo = new StreamFifoCC(io.node.cmd.cmdStream.payload,Depth,nodeDomain,busDomain)
  val rspFifo = new StreamFifoCC(io.node.rsp.rspStream.payload,Depth,busDomain,nodeDomain)
  val wrFifo = new StreamFifoCC(io.node.wrChannel.wrStream.payload,Depth,busDomain,nodeDomain)
  val rdFifo = new StreamFifoCC(io.node.rdChannel.rdStream.payload,Depth,busDomain,nodeDomain)

  cmdFifo.io.push <> io.node.cmd.cmdStream
  cmdFifo.io.pop <> io.nodeOut.cmd.cmdStream

  rspFifo.io.push <> io.nodeOut.rsp.rspStream
  rspFifo.io.pop <> io.node.rsp.rspStream

  rdFifo.io.push <> io.nodeOut.rdChannel.rdStream
  rdFifo.io.pop <> io.node.rdChannel.rdStream

  wrFifo.io.push <> io.node.wrChannel.wrStream
  wrFifo.io.pop <> io.nodeOut.wrChannel.wrStream

}
