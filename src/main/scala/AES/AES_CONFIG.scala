package AES

import spinal.core._
import spinal.lib._
import spinal.lib.{Flow, IMasterSlave, Stream, master, slave}

object AESMode {
  val AES_128 = U(0,2 bits)
  val AES_192 = U(1,2 bits)
  val AES_256 = U(2,2 bits)
}


object AES_CONFIG {
  def blockWidth  = 128 bits


  def rcon(keyWidth: BitCount): List[Int] = {
    val rconValue = List(
      0x8d, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a,
      0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91, 0x39,
      0x72, 0xe4, 0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33, 0x66, 0xcc, 0x83, 0x1d, 0x3a
    )
    if (keyWidth == 128) {
      return rconValue.take(10 + 1)
    } else if (keyWidth == 192) {
      return rconValue.take(12 + 1)
    } else {
      return rconValue.take(14 + 1)
    }

  }

  def shiftRowIndex = List(
    4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11, 0, 5, 10, 15
  )

  private def xtime(xtime_input:Bits):Bits = {
    val xtime_output = Bits(8 bits)
    xtime_output(7)	:=	xtime_input(6);
    xtime_output(6)	:=	xtime_input(5);
    xtime_output(5)	:=	xtime_input(4);
    xtime_output(4)	:=	xtime_input(7) ^ xtime_input(3);
    xtime_output(3)	:=	xtime_input(7) ^ xtime_input(2);
    xtime_output(2)	:=	xtime_input(1);
    xtime_output(1)	:=	xtime_input(7) ^ xtime_input(0);
    xtime_output(0)	:=	xtime_input(7);
    xtime_output
  }

  private def MixColumn8(in:Vec[Bits]):Bits = {
    val w1,w2,w3,outx_var = Bits(8 bits)
    w1		:=	in(3)^in(2)
    w2		:=	in(1)^in(0)
	  w3		:=	xtime(w1)
	  outx_var	:=	in(2)^w2^w3
    outx_var
  }

  def MixColumn32(mixcolumn32_in:Bits):Bits = {
    val valmixcolumn32_out = Bits(32 bits)

    //valmixcolumn32_out(7 downto 0) := MixColumn8((mixcolumn32_in.rotateRight(8).subdivideIn(8 bits)))
    for(i <- (0 to 3)) {
      valmixcolumn32_out(i*8,8 bits) := MixColumn8((mixcolumn32_in.rotateRight(8*(i+1)).subdivideIn(8 bits)))
    }
    valmixcolumn32_out
  }


  def gFunc(Rkey_in:Bits, Rcon:Bits):Bits = {
    val wordVec = Rkey_in.rotateLeft(8)
    val KeyNew = Vec(Bits(8 bits),4)
    val VecTmp = Sbox.Sbox32(wordVec).subdivideIn(8 bits)
    KeyNew(3) := VecTmp(3) ^ Rcon
    KeyNew(2) := VecTmp(2)
    KeyNew(1) := VecTmp(1)
    KeyNew(0) := VecTmp(0)
    KeyNew.asBits
  }

  def hFunc(Rkey_in:Bits):Bits = {
    val result = Sbox.Sbox32(Rkey_in)
    return result
  }
}

case class MyroundFunction(twoRound:Boolean = false) extends Component{
  val io = new Bundle {
    val roundData = in Bits(128 bits)
    val roundKey = if (twoRound) {
      in Bits(256 bits)
    } else {
      in Bits(128 bits)
    }
    val needMix = in Bool()
    val result = out Bits(128 bits)
  }
  def roundFunction(roundData:Bits,needMix:Bool):Bits = {
    val sboxData  = Vec(Bits(32 bits), 4)
    val byteReplaceData  = Vec(Bits(32 bits), 4)
    val dataState  = Vec(Bits(32 bits), 4)

    val shiftTmpByte = Vec(Bits(8 bits), 16)


    sboxData := (roundData).subdivideIn(32 bits)

    for (i <- 0 to sboxData.length-1) {
      byteReplaceData(i) := Sbox.Sbox32(sboxData(i))
    }

    val tmpByte = byteReplaceData.asBits.subdivideIn(8 bits)

    for ((src, dst) <- AES_CONFIG.shiftRowIndex.zipWithIndex){
      shiftTmpByte(dst) := tmpByte(src)
    }

    val afterShift = Vec(Bits(32 bits), 4)
    afterShift := shiftTmpByte.asBits.subdivideIn(32 bits)


    when(needMix) {
      for (i <- 0 to afterShift.length-1) {
        dataState(i) := AES_CONFIG.MixColumn32(afterShift(i))
      }
    } otherwise {
      for (i <- 0 to afterShift.length-1) {
        dataState(i) := afterShift(i)
      }
    }
    return dataState.asBits
  }

  if (twoRound) {
    val firstRound = roundFunction(io.roundData, True)  ^ io.roundKey(127 downto 0)
    report(Seq("io.roundData:=\t",io.roundData.asUInt,"\tkey:\t",io.roundKey(127 downto 0),"\tresult:=\t",firstRound.asUInt))
    io.result := roundFunction(firstRound , io.needMix) ^ io.roundKey(255 downto 128)
    report(Seq("io.roundData:=\t",firstRound.asUInt,"\tkey:\t",io.roundKey(255 downto 128),"\tresult:=\t",io.result.asUInt))
  } else {
    io.result := roundFunction(io.roundData  , io.needMix) ^ io.roundKey
    report(Seq("io.roundData:=\t",io.roundData.asUInt,"\tkey:\t",io.roundKey,"\tresult:=\t",io.result.asUInt))
  }

  def driveFrom(roundData:Bits,roundKey:Bits,needMix:Bool):Bits = {
    io.roundData := roundData
    io.roundKey := roundKey
    io.needMix := needMix
    io.result
  }
}





/**
  * Symmetric Crypto block generiics
  * @param keyWidth   Key width
  * @param blockWidth Block width
  * @param useEncDec  Create a signal for the encryption/decryption
  */
case class CryptoBlockConfig(
                               keyWidth   : BitCount,
                               blockWidth : BitCount,
                               useEncDec  : Boolean = true
                             )


case class coreDataIn(config: CryptoBlockConfig) extends Bundle {
  val mode   = UInt(2 bits)
  val key    = Bits(config.keyWidth)
  val block  = Bits(config.blockWidth)
  val enc    = if(config.useEncDec) Bool else null
}

case class coreRsp(config: CryptoBlockConfig) extends Bundle {
  val block  = Bits(config.blockWidth)
}

case class CryptoBlockIO(config: CryptoBlockConfig) extends Bundle with IMasterSlave {

  val cmd = Stream(coreDataIn(config))
  val rsp = Flow(coreRsp(config))

  override def asMaster() = {
    master(cmd)
    slave(rsp)
  }
}
