package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.AhbLite3Config
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class  testerArb(c:DmaCfg, ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val testStream = Vec(slave(BRAM(BRAMConfig(ahbCfg.dataWidth, ahbCfg.addressWidth))),c.slaveNode)
    val finalTestStream = master(BRAM(BRAMConfig(ahbCfg.dataWidth, ahbCfg.addressWidth)))
  }

  val reqArray = for (i <- io.testStream) yield i.en
  val maskLocked = Reg(Bits(c.slaveNode bits))
  val maskProposal = Bits(c.slaveNode bits)
  maskProposal := OHMasking.roundRobin(reqArray.asBits(), maskLocked(maskLocked.high - 1 downto 0) ## maskLocked.msb)

  maskLocked := maskProposal

  for (i <- io.testStream) {
    i.rddata := 0
  }
  io.finalTestStream.en := False
  io.finalTestStream.we := 0
  io.finalTestStream.wrdata := 0
  io.finalTestStream.addr := 0

  for ((input, requestRouted) <- (io.testStream, maskProposal.asBools).zipped) {
    when(requestRouted) {
      input <> io.finalTestStream
    }
  }
}
