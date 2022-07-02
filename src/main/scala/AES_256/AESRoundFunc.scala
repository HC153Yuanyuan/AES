package AES_256

import spinal.core._

case class AESRoundFunc(twoRound:Boolean = false) extends Component{
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

    report(Seq("byteReplaceData\t",byteReplaceData.asBits))

    val tmpByte = byteReplaceData.asBits.subdivideIn(8 bits)

    for ((src, dst) <- AES.shiftRowIndex.zipWithIndex){
      shiftTmpByte(dst) := tmpByte(src)
    }

    val afterShift = Vec(Bits(32 bits), 4)
    afterShift := shiftTmpByte.asBits.subdivideIn(32 bits)

    report(Seq("afterShift\t",afterShift.asBits))

    when(needMix) {
      for (i <- 0 until afterShift.length) {
        dataState(i) := AES.MixColumn32(afterShift(i))
      }
    } otherwise {
      for (i <- 0 until afterShift.length) {
        dataState(i) := afterShift(i)
      }
    }
    report(Seq("afterMix\t",dataState.asBits))
    return dataState.asBits
  }

  if (twoRound) {
    val firstRound = roundFunction(io.roundData, True)  ^ io.roundKey(127 downto 0)
    io.result := roundFunction(firstRound , io.needMix) ^ io.roundKey(255 downto 128)
  } else {
    io.result := roundFunction(io.roundData  , io.needMix) ^ io.roundKey
    report(Seq("io.roundData\t",io.roundData,"\tio.roundKey\t",io.roundKey,"\tio.result\t",io.result))
  }

  def driveFrom(roundData:Bits,roundKey:Bits,needMix:Bool):Bits = {
    io.roundData := roundData
    io.roundKey := roundKey
    io.needMix := needMix
    io.result
  }
}




