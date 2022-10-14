package DMA

import spinal.core._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Arbiter, AhbLite3Config, AhbLite3OnChipRam}
import spinal.lib._
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class TestWrapper(c:DmaCfg, ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val sysBusClk = in Bool()
    val sysBusRst = in Bool()
    val nodeClk  = in Bool()
    val nodeRst = in Bool()
    val dmaEnable = in Bool()
    val nodeStream = Vec(slave(DmaNodeInf(c,NodeType.fullVersion)),c.slaveNode)
    val testStream = Vec(slave(BRAM(BRAMConfig(ahbCfg.dataWidth,ahbCfg.addressWidth))),c.slaveNode)
  }


  val busDomain = ClockDomain(io.sysBusClk,io.sysBusRst,config = ClockDomainConfig(clockEdge = RISING,resetKind = ASYNC,resetActiveLevel = LOW))
  val nodeDomain = ClockDomain(io.nodeClk,io.nodeRst,config = ClockDomainConfig(clockEdge = RISING,resetKind = ASYNC,resetActiveLevel = LOW))



  val dma =  DmaTop(c,ahbCfg)
  val busArea  = new ClockingArea(busDomain) {
    val AhbRam = dmaRam(ahbCfg, byteCount= 1 << ahbCfg.addressWidth,c.slaveNode)
  }

  dma.io.nodeStream <> io.nodeStream
  dma.io.ahbBus <> busArea.AhbRam.io.ahb
  dma.io.dmaEnable := io.dmaEnable
  dma.io.nodeClk := io.nodeClk
  dma.io.nodeRst := io.nodeRst
  dma.io.sysBusRst := io.sysBusRst
  dma.io.sysBusClk := io.sysBusClk

  busArea.AhbRam.io.mem <> io.testStream
}