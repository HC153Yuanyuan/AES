package DMA

import spinal.core._
import spinal.lib._

case class MacDmaConfig(addressWidth : Int,
                        dataWidth : Int,
                        burstCountWidth : Int,
                        enableTx: Boolean,
                        enableRx: Boolean)

case class DmaCmd(c:MacDmaConfig) extends Bundle {
  val startAddr = UInt(c.addressWidth bit)
  val transSize = UInt(c.burstCountWidth bit)
  val readCmd = if (c.enableRx == false) False else Bool()
}



case class MacDmaChannelCC(c:MacDmaConfig) extends Component {
  val io = new Bundle {
    val clk = in Bool()
    val socclk = in Bool()
    val resetn = in Bool()
    val socresetn = in Bool()
    val cmdFromLocal = slave Stream(DmaCmd(c))
    val cmdToBus = master Stream(DmaCmd(c))

    val txPayloadFromLocal =if (c.enableTx == true) slave Stream(UInt(c.dataWidth bits)) else null
    val txPayloadToBus = if (c.enableTx == true) master Stream(UInt(c.dataWidth bits)) else null

    val rxPayloadToLocal = if (c.enableRx == true) master Stream(UInt(c.dataWidth bits)) else null
    val rxPayloadFromBus = if (c.enableRx == true) slave Stream(UInt(c.dataWidth bits)) else null

  }

  val socresetn = BufferCC(io.resetn)

  val macClkDomain = ClockDomain(
    clock  = io.clk,
    reset  = io.resetn,
    config = ClockDomainConfig(
      clockEdge        = RISING,
      resetKind        = ASYNC,
      resetActiveLevel = LOW
    )
  )

  val socClkDomain = ClockDomain(
    clock  = io.socclk,
    reset  = io.socresetn,
    config = ClockDomainConfig(
      clockEdge        = RISING,
      resetKind        = ASYNC,
      resetActiveLevel = LOW
    )
  )

  val cmdFifo = StreamFifoCC(DmaCmd(c),2,macClkDomain,socClkDomain)
  cmdFifo.io.pop <> io.cmdToBus
  cmdFifo.io.push <> io.cmdFromLocal

  if (c.enableRx == true) {
    val rxFifo = StreamFifoCC(UInt(c.dataWidth bits),2,socClkDomain,macClkDomain)
    rxFifo.io.pop <> io.rxPayloadToLocal
    rxFifo.io.push <> io.rxPayloadFromBus
  }


  if (c.enableTx == true) {
    val txFifo = StreamFifoCC(UInt(c.dataWidth bits),2,macClkDomain,socClkDomain)
    txFifo.io.pop <> io.txPayloadToBus
    txFifo.io.push <> io.txPayloadFromLocal
  }
}


