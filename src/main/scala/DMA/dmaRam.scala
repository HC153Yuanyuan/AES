package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config}
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class dmaRam(AhbLite3Config:AhbLite3Config, byteCount: BigInt) extends Component {

  val io = new Bundle {
    val ahb = slave(AhbLite3(AhbLite3Config))
    val mem = slave(BRAM(BRAMConfig(AhbLite3Config.dataWidth,AhbLite3Config.addressWidth)))
  }

  val wordCount = byteCount / AhbLite3Config.bytePerWord
  val ram       = Mem(AhbLite3Config.dataType, wordCount.toInt)
  val wordRange = log2Up(wordCount) + log2Up(AhbLite3Config.bytePerWord) - 1 downto log2Up(AhbLite3Config.bytePerWord)

  io.mem.rddata := 0
  when(io.mem.en) {
    when(io.mem.we =/= 0) {
      ram.write(io.mem.addr(wordRange),io.mem.wrdata,True,io.mem.we)
    } otherwise {
      io.mem.rddata := ram.readSync(io.mem.addr(wordRange))
    }
  }
  // Address/control phase to write data phase
  val pendingWrite = Reg(new Bundle{
    val valid   = Bool()
    val address = ram.addressType()
    val mask    = Bits(AhbLite3Config.bytePerWord bits)
  })

  pendingWrite.valid init(False)
  pendingWrite.valid := False

  when(io.ahb.HREADY){
    pendingWrite.valid   := io.ahb.HSEL && io.ahb.HTRANS(1) && io.ahb.HWRITE
    pendingWrite.address := io.ahb.HADDR(wordRange)
    pendingWrite.mask    := io.ahb.writeMask
  }

  io.ahb.setOKEY

  // Avoid write to read hazards
  io.ahb.HREADYOUT := !(io.ahb.HSEL && io.ahb.HTRANS(1) && !io.ahb.HWRITE && pendingWrite.valid && io.ahb.HADDR(wordRange) === pendingWrite.address)
  io.ahb.HRDATA    := ram.readSync(
    address = io.ahb.HADDR(wordRange),
    enable  = io.ahb.HSEL && io.ahb.HTRANS(1) && !io.ahb.HWRITE && io.ahb.HREADY
  )

  ram.write(
    enable  = pendingWrite.valid,
    address = pendingWrite.address,
    mask    = pendingWrite.mask,
    data    = io.ahb.HWDATA
  )
}
