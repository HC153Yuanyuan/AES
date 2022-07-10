package DataStream

import spinal.core._
import spinal.lib._


case class MacWordToByte() extends Component {
  val io = new Bundle {
    // data stream
    val payload_in = slave(Stream(Payload()))
    val payload_out = master(Stream(Bits(8 bits)))

    //cfg
    val flush = in Bool()
  }

  val stashFifo = new StreamFifo(Bits(8 bits),5)
  val convertData = RegInit(False)
  val curByte:UInt = U(0,3 bits)
  val curByteLatch = RegNextWhen(curByte,io.payload_in.fire) init 0
  val curIndex = Reg(UInt(2 bits)) init 0
  val dataLatch = RegNextWhen(io.payload_in.data,io.payload_in.fire) init 0

  switch(io.payload_in.byteEna.asUInt) {
    is(0) {
      curByte := U(0,3 bits)
    }
    is(1) {
      curByte := U(1,3 bits)
    }
    is(3) {
      curByte := U(2,3 bits)
    }
    is(7) {
      curByte := U(3,3 bits)
    }
    is(15) {
      curByte := U(4,3 bits)
    }
    default {
      assert(True,"byteEna Error!")
    }
  }



  io.payload_in.ready := (stashFifo.io.occupancy <= 1) && ~convertData
  stashFifo.io.push.valid := False
  stashFifo.io.push.payload := 0
  when(io.flush) {
    convertData := False
    curIndex := 0
  } elsewhen(io.payload_in.fire) {
    when(curByte =/=1) {
      convertData := True
      curIndex := 1
    } otherwise {
      convertData := False
      curIndex := 0
    }
    stashFifo.io.push.valid := True
    stashFifo.io.push.payload := io.payload_in.data(7 downto 0)
  } elsewhen(convertData) {
    when((curIndex + 1) =/= curByteLatch) {
      convertData := True
      curIndex := curIndex + 1
    } otherwise {
      convertData := False
      curIndex := 0
    }
    stashFifo.io.push.valid := True
    stashFifo.io.push.payload := (dataLatch |>> curIndex*8)(7 downto 0)
  }

  stashFifo.io.pop <> io.payload_out
}

