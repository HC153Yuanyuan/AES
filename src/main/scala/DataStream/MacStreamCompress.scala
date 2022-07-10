package DataStream

import spinal.core._
import spinal.lib._

case class MacStreamCompress()  extends Component {
  val io = new Bundle {
    // data stream
    val payload_in = slave(Flow(Payload()))
    val payload_out = master(Flow(Payload()))

    //cfg
    val flush = in Bool()
    val lastTrans = in Bool()
  }


  val curByte:UInt = U(0,3 bits)

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

  val remainByte = Reg(UInt(2 bits)) init 0
  val remainPayload = Reg(Bits(24 bits)) init 0
  val nxtremainByte = remainByte + curByte


  val inputDataAlignLeft = (io.payload_in.data ## (remainPayload |<< (3 - remainByte)*8))    |<< ((4 - curByte)*8 )

  when(io.flush) {
    remainByte := 0
    remainPayload :=  B(0,24 bits)
  } elsewhen(io.payload_in.fire) {
      remainByte := (nxtremainByte).resize(remainByte.getWidth)
      switch(nxtremainByte.resize(2 bits)) {
        is(1) {
          remainPayload := inputDataAlignLeft(55 downto 48).resize(24 bits)
        }
        is(2) {
          remainPayload := inputDataAlignLeft(55 downto 40).resize(24 bits)
        }
        is(3) {
          remainPayload := inputDataAlignLeft(55 downto 32)
        }
      }
  }

  val dataStore = B(0,32 bits)


  when(io.lastTrans) {
    dataStore := remainPayload.resize(32 bits)
  } otherwise {
    switch(remainByte) {
      is(0) {
        dataStore := io.payload_in.data
      }
      is(1) {
        dataStore := io.payload_in.data(23 downto 0) ## remainPayload(7 downto 0)
      }
      is(2) {
        dataStore := io.payload_in.data(15 downto 0) ##  remainPayload(15 downto 0)
      }
      is(3) {
        dataStore := io.payload_in.data(7 downto 0) ## remainPayload(23 downto 0)
      }
    }
  }

  val byteEnaStore = Bits(4 bits)

  def genBitmap(num:UInt):Bits = {
    val result:Bits = B(0,4 bits)
    when(num === 0) {
      result := 0
    } elsewhen(num === 1) {
      result := 1
    } elsewhen(num === 2) {
      result := 3
    } elsewhen(num === 3) {
      result := 7
    }
    result
  }

  when(io.lastTrans) {
    byteEnaStore := genBitmap(remainByte)
  } otherwise {
    byteEnaStore := 0xf
  }

  io.payload_out.payload.data := dataStore
  io.payload_out.valid := io.payload_in.fire && nxtremainByte.msb || (io.lastTrans && remainByte =/= 0)
  io.payload_out.byteEna := byteEnaStore

}
