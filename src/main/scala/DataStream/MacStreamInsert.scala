package DataStream

import spinal.core._
import spinal.lib._

case class Payload() extends Bundle {
  val data = Bits(32 bits)
  val byteEna = Bits(4 bits)
}


case class MacStreamInsert() extends Component {
  val io = new Bundle {

    // data stream
    val payload_in = slave(Stream(Payload()))
    val payload_out = master(Stream(Payload()))

    //cfg
    val flush = in Bool()
    val lastTrans = in Bool()
    val insertEnable = in Bool()
    val insertByte = in UInt(6 bits)
  }

  val byteCnt = Reg(UInt(6 bits)) init 0

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

  val nxtByteCnt = byteCnt + curByte

  val insertByteFound = io.insertEnable && (byteCnt < io.insertByte) && (nxtByteCnt >= io.insertByte)

  val separateIndex= nxtByteCnt - io.insertByte


  val remainByte = Reg(UInt(2 bits)) init 0
  val remainPayload = Reg(Bits(24 bits)) init 0
  val nxtremainByte = remainByte + curByte
  val needInsert = insertByteFound && separateIndex =/= 0

  val firstWord = Bits(32 bits)
  val remainWord = Bits(24 bits)


  switch(separateIndex.resize(2 bits)) {
    is(1) {
      firstWord := io.payload_in.data(23 downto 0).resize(32 bits)
      remainWord := io.payload_in.data(31 downto 24).resize(24 bits)
    }
    is(2) {
      firstWord := io.payload_in.data(15 downto 0).resize(32 bits)
      remainWord := io.payload_in.data(31 downto 16).resize(24 bits)
    }
    is(3) {
      firstWord := io.payload_in.data(7 downto 0).resize(32 bits)
      remainWord := io.payload_in.data(31 downto 8).resize(24 bits)
    }
    default {
      firstWord := io.payload_in.data
      remainWord := B(0,24 bits)
    }
  }

  when(io.flush) {
    byteCnt := 0
  } elsewhen((byteCnt <= 59) && io.payload_in.fire) {
    byteCnt :=  nxtByteCnt
  }



  val inputDataAlignLeft = (io.payload_in.data ## (remainPayload |<< (3 - remainByte)*8))    |<< ((4 - curByte)*8 )
  val dataStore = B(0,32 bits)

  when(io.flush) {
    remainByte := 0
    remainPayload :=  B(0,24 bits)
  } elsewhen(io.payload_in.fire) {
    when(needInsert) {
      remainByte := separateIndex.resize(remainByte.getWidth)
      remainPayload := remainWord
    } .otherwise {
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
  }


//  dataStore := B(0,32 bits)
  when(io.lastTrans) {
    dataStore := remainPayload.resize(32 bits)
  } elsewhen(io.payload_in.fire && needInsert) {
    dataStore := firstWord
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
  } elsewhen(io.payload_in.fire && needInsert) {
    byteEnaStore := genBitmap(curByte-separateIndex.resize(2 bits))
  } otherwise {
    byteEnaStore := 0xf
  }


  val stashFifo = new StreamFifo(Payload(),2)

  stashFifo.io.flush := io.flush

  stashFifo.io.push.valid := io.payload_in.fire && nxtremainByte.msb || (io.lastTrans && remainByte =/= 0)
  stashFifo.io.push.payload.data := dataStore
  stashFifo.io.push.payload.byteEna := byteEnaStore
  io.payload_in.ready := stashFifo.io.occupancy === 0


  io.payload_out <> stashFifo.io.pop

}
