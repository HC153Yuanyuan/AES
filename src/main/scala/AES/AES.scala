package AES
import spinal.core._

object AES {
  def blockWidth  = 128 bits
  def enable2Round = false
//  def nbrRound(keySize: BitCount): Int = keySize.value match {
//    case 128 => 10
//    case 192 => 12
//    case 256 => 14
//    case _   => SpinalError(s"AES doesn't support the following key size $keySize")
//  }
//

//  def rcon(keyWidth: BitCount): List[Int] = {
//    val rconValue = List(
//      0x8d, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a,
//      0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91, 0x39,
//      0x72, 0xe4, 0xd3, 0xbd, 0x61, 0xc2, 0x9f, 0x25, 0x4a, 0x94, 0x33, 0x66, 0xcc, 0x83, 0x1d, 0x3a
//    )
//    return rconValue.take(AES.nbrRound(keyWidth) + 1)
//  }


  def shiftRowIndex = List(
    0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11
  )

  def invShiftRowIndex = List(
    0, 13, 10, 7, 4, 1, 14, 11, 8, 5, 2, 15, 12, 9, 6, 3
  )
}
