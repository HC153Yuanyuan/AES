package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config}
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class DmaTop(c:DmaCfg, ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val nodeStream = Vec(slave(DmaNodeInf(c,NodeType.fullVersion)),c.slaveNode)
    val sysBusClk = in Bool()
    val sysBusRst = in Bool()
    val nodeClk  = in Bool()
    val nodeRst = in Bool()
    val ahbBus = master(AhbLite3(ahbCfg))
    val dmaEnable = in Bool()
  }
  val busDomain = ClockDomain(
    clock = io.sysBusClk,
    reset = io.sysBusRst,
    config = ClockDomainConfig(
      clockEdge = RISING,
      resetKind = ASYNC,
      resetActiveLevel = LOW
    )
  )

  val nodeDomain = ClockDomain(
    clock = io.nodeClk,
    reset = io.nodeRst,
    config = ClockDomainConfig(
      clockEdge = RISING,
      resetKind = ASYNC,
      resetActiveLevel = LOW
    )
  )

  val nodeArea = new ClockingArea(nodeDomain) {
    val dmaArb = DmaArbWrapper(c)
    val dmaFifo = DmaFifo(c,4,8,true)

    dmaArb.io.nodeList <> io.nodeStream
    dmaArb.io.finalNode <> dmaFifo.io.node
    dmaArb.io.dmaEnable := io.dmaEnable
    dmaArb.io.dmaReady := dmaFifo.io.node.cmd.cmdStream.ready
  }

  val busArea = new ClockingArea(busDomain) {
    val dma2ahb = Dma2AHB(c, ahbCfg)
    val dmaFifo = DmaFifo(c,2,2,true)
    dma2ahb.io.dmaNode <> dmaFifo.io.nodeOut
    dma2ahb.io.ahbBus <> io.ahbBus
    dma2ahb.io.ahbAllowMaxBurst := 16
  }

  val dmaCdc = DmaCdc(c,4,nodeDomain, busDomain)
  dmaCdc.io.node <> nodeArea.dmaFifo.io.nodeOut
  dmaCdc.io.nodeOut <> busArea.dmaFifo.io.node
}
