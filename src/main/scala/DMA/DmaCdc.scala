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

  val cmdInfo = io.node.cmd.startAddr ## io.node.cmd.wrOp ## io.node.cmd.transLen ## io.node.cmd.pri


  val cmdFifo = new StreamFifoCC(cmdInfo,Depth,nodeDomain,busDomain)
  val rspFifo = new StreamFifoCC(io.node.cmd.rspStream.payload,Depth,busDomain,nodeDomain)
  val wrFifo = new StreamFifoCC(io.node.wrChannel.wrStream.payload,Depth,busDomain,nodeDomain)
  val rdFifo = new StreamFifoCC(io.node.rdChannel.rdStream.payload,Depth,busDomain,nodeDomain)

  cmdFifo.io.push.payload := cmdInfo
  cmdFifo.io.push.valid := io.node.cmd.reqVld
  io.node.cmd.reqReady := cmdFifo.io.push.ready

  (io.nodeOut.cmd.startAddr ## io.nodeOut.cmd.wrOp ## io.nodeOut.cmd.transLen ## io.nodeOut.cmd.pri).assignFromBits(cmdFifo.io.pop.payload.asBits)
  io.nodeOut.cmd.reqVld := cmdFifo.io.pop.valid
  cmdFifo.io.pop.ready := io.nodeOut.cmd.reqReady

  rspFifo.io.push <> io.nodeOut.cmd.rspStream
  rspFifo.io.pop <> io.node.cmd.rspStream

  rdFifo.io.push <> io.nodeOut.rdChannel.rdStream
  rdFifo.io.pop <> io.node.rdChannel.rdStream

  wrFifo.io.push <> io.node.wrChannel.wrStream
  wrFifo.io.pop <> io.nodeOut.wrChannel.wrStream

}
