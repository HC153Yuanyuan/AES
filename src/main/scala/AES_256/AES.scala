package AES_256

import spinal.core._
import spinal.lib._

object AES_CONFIG {
  def blockWidth = 128 bits
  def enable2Round = true
  def keyWidth:BitCount = 256 bits
}


object AESMODE extends SpinalEnum{
  val AES128 = newElement()
  val AES256 = newElement()
}



object AES {
  def shiftRowIndex = List(
    0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11
  )

  def invShiftRowIndex = List(
    0, 13, 10, 7, 4, 1, 14, 11, 8, 5, 2, 15, 12, 9, 6, 3
  )

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

  private def MixColumn8(in:Seq[Bits]):Bits = {
    val w1,w2,w3,outx_var = Bits(8 bits)
    w1		:=	in(1)^in(0)
    w2		:=	in(3)^in(2)
    w3		:=	xtime(w1)
    outx_var	:=	in(1)^w2^w3
    outx_var
  }

  def MixColumn32(mixcolumn32_in:Bits):Bits = {
    val valmixcolumn32_out = Bits(32 bits)

    for(i <- (0 to 3)) {
      valmixcolumn32_out(i*8,8 bits) := MixColumn8((mixcolumn32_in.rotateRight(8*i).subdivideIn(8 bits)))
    }
    valmixcolumn32_out
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
