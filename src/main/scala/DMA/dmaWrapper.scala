package DMA

import spinal.core._
import spinal.lib.bus.amba3.ahblite.{AhbLite3Arbiter, AhbLite3Config, AhbLite3OnChipRam}
import spinal.lib._
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class dmaWrapper(c:DmaCfg, ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val nodeStream = Vec(master(DmaNodeInf(c,NodeType.fullVersion)),c.slaveNode)
    val testStream = Vec(slave(BRAM(BRAMConfig(ahbCfg.dataWidth,ahbCfg.addressWidth))),c.slaveNode)
  }


  val testArb = testerArb(c,ahbCfg)
  val dmaArb = DmaArbiter(c)
  val dma =  DmaCtrl(c,ahbCfg)
  val AhbRam = dmaRam(ahbCfg, byteCount= 1 << ahbCfg.addressWidth)
  val ramInitData = (for (i <- 0 until (1 << ahbCfg.addressWidth)/4) yield B(i,ahbCfg.dataWidth bits)).toSeq
  AhbRam.ram.init(ramInitData)

  for(i <- 0 until c.slaveNode) {
    io.nodeStream(i) <> dmaArb.io.nodeList(i)
    testArb.io.testStream(i) <> io.testStream(i)
  }
  dma.io.dmaNode <> dmaArb.io.finalNode
  dma.io.ahbBus <> AhbRam.io.ahb
  AhbRam.io.mem <> testArb.io.finalTestStream
}