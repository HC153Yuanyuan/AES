package DMA

import spinal.core._
import spinal.core.sim.SimDataPimper
import spinal.lib._
import spinal.lib.bus.amba3._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config}
import spinal.lib.bus.bram.{BRAM, BRAMConfig}

case class dmaRam(AhbLite3Config:AhbLite3Config, byteCount: BigInt,memInfNum:Int) extends Component {

  val io = new Bundle {
    val ahb = slave(AhbLite3(AhbLite3Config))
    val mem = Vec(slave(BRAM(BRAMConfig(AhbLite3Config.dataWidth,AhbLite3Config.addressWidth))),memInfNum)
  }

  val wordCount = byteCount / AhbLite3Config.bytePerWord
  val ram       = Mem(AhbLite3Config.dataType, wordCount.toInt)
  val wordRange = log2Up(wordCount) + log2Up(AhbLite3Config.bytePerWord) - 1 downto log2Up(AhbLite3Config.bytePerWord)
  val forceReadyDown = RegInit(False) simPublic()
  for(memio <- io.mem) {
    memio.rddata := 0
    when(memio.en) {
      when(memio.we =/= 0) {
        ram.write(memio.addr(wordRange),memio.wrdata,True,memio.we)
      } otherwise {
        memio.rddata := ram.readAsync(memio.addr(wordRange))
      }
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

  when(io.ahb.HREADY && io.ahb.HTRANS(1)){
    pendingWrite.valid   := io.ahb.HSEL && io.ahb.HTRANS(1) && io.ahb.HWRITE
    pendingWrite.address := io.ahb.HADDR(wordRange)
    pendingWrite.mask    := io.ahb.writeMask
  }

  io.ahb.setOKEY

  // Avoid write to read hazards
  io.ahb.HREADYOUT :=   !forceReadyDown && !(io.ahb.HSEL && io.ahb.HTRANS(1) && !io.ahb.HWRITE && pendingWrite.valid && io.ahb.HADDR(wordRange) === pendingWrite.address)
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
  when(pendingWrite.valid){
    report(Seq("write addr is\t", pendingWrite.address, "\twrite data is\t" , io.ahb.HWDATA))
  }
}
