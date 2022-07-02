package AES_256

import spinal.core._
import spinal.lib._
case class AESKeyGen(keyWidth:BitCount) extends Component {
  val io = new Bundle {
    val initKey = in Bits (keyWidth.value bits)
    val keyMode = in UInt (2 bits)
    val startGetKey = in Bool()
    val roundNum = in UInt (4 bits)
    val curKey = out(if (AES_CONFIG.enable2Round) Bits(256 bits) else Bits(128 bits))
  }

  val keyStashReg = Reg(Bits(keyWidth.value bits))
  val keyRcon = Mem(Bits(8 bits), AES.rcon(keyWidth).map(B(_, 8 bits)))
  val cntStage = Reg(UInt(4 bits)) init(0)
  val keySbox = new Sbox32()

  keySbox.driveFrom(0)

  def gFunc(Rkey_in:Bits, Rcon:Bits):Bits = {
    val wordVec = Rkey_in.rotateRight(8)
    val KeyNew = Vec(Bits(8 bits),4)
    val VecTmp = Sbox.Sbox32(wordVec).subdivideIn(8 bits)
    KeyNew(0) := VecTmp(0) ^ Rcon
    KeyNew(1) := VecTmp(1)
    KeyNew(2) := VecTmp(2)
    KeyNew(3) := VecTmp(3)
    KeyNew.asBits
  }

  def hFunc(Rkey_in:Bits):Bits = {
    val result = Sbox.Sbox32(Rkey_in)
    return result
  }

  /*
  *
  * */
  def genRoundKeyBase(lastRoundKey:Bits,roundNum:UInt,cntStage:UInt,keyMode:UInt):Vec[Bits] = {
    val newKey = Vec(Bits(32 bits),keyWidth.value / 32)
    val lastKey = lastRoundKey.subdivideIn(32 bits)



  //  report(Seq(lastRoundKey,"\t",roundNum,"\t",cntStage,"\t"))
  //  report(Seq("roundNum == 1",lastKey(0),"\t",gFunc(Rkey_in=lastKey(7),keyRcon(cntStage)),"\t",newKey(0)))
  //  report(Seq("roundNum == 0",lastKey(4),"\t",hFunc(Rkey_in=lastKey(3)),"\t",newKey(4)))
   if (keyWidth.value == 256) {
      newKey := lastKey
      when(keyMode === AESMODE.AES256.asBits.asUInt) {
        when(roundNum(0) === True) {
          newKey(0) := lastKey(0) ^ gFunc(Rkey_in=lastKey(7),keyRcon(cntStage))
          newKey(1) := lastKey(1) ^ newKey(0)
          newKey(2) := lastKey(2) ^ newKey(1)
          newKey(3) := lastKey(3) ^ newKey(2)
        } otherwise {
          newKey(4) := lastKey(4) ^ hFunc(Rkey_in=lastKey(3))
          newKey(5) := lastKey(5) ^ newKey(4)
          newKey(6) := lastKey(6) ^ newKey(5)
          newKey(7) := lastKey(7) ^ newKey(6)
        }
      } otherwise {
        newKey(0) := lastKey(0) ^ gFunc(Rkey_in=lastKey(3),keyRcon(roundNum))
        newKey(1) := lastKey(1) ^ newKey(0)
        newKey(2) := lastKey(2) ^ newKey(1)
        newKey(3) := lastKey(3) ^ newKey(2)
      }
      newKey
    } else if(keyWidth.value == 128) {
      newKey(0) := lastKey(0) ^ gFunc(Rkey_in=lastKey(3),keyRcon(roundNum))
      newKey(1) := lastKey(1) ^ newKey(0)
      newKey(2) := lastKey(2) ^ newKey(1)
      newKey(3) := lastKey(3) ^ newKey(2)
      newKey
    } else {
      newKey.foreach(_ := 0)
      newKey
    }
  }

  // get the round key from 256 bits key
  def getKey(keyMode:UInt,roundNum_0:Bool,newKey:Vec[Bits]):Bits = {
    val returnKey = Bits(128 bits)
    if(keyWidth.value == 256) {
      when(keyMode === AESMODE.AES256.asBits.asUInt) {
        returnKey := roundNum_0.mux(
          False  -> newKey.slice(0,4).asBits,
          True  -> newKey.slice(4,8).asBits
        )
      } otherwise {
        returnKey := newKey.slice(0,4).asBits
      }
      returnKey
    } else {
      returnKey := newKey.slice(0,4).asBits
      returnKey
    }
  }

  def genRoundKey(firstRound:Bool,lastRoundKey:Bits,roundNum:UInt,keyMode:UInt) = {
    val newKey:Vec[Bits] = Vec(Bits(32 bits), keyWidth.value/32)
    val nxtCntStage = UInt(4 bits)

    if (!AES_CONFIG.enable2Round) {
      val returnKey = Bits(128 bits)
      when(firstRound) {
        cntStage := 1
      } .elsewhen((roundNum(0) === False) ) {
        cntStage := cntStage + 1
      }
      newKey := genRoundKeyBase(lastRoundKey,roundNum,firstRound?U(1)|cntStage,keyMode)
      returnKey := getKey(keyMode,roundNum(0),newKey)
      keyStashReg := newKey.asBits
      returnKey
    } else {
      val firstKey = Bits(128 bits)
      val secKey = Bits(128 bits)
      val newKeyTmp :Vec[Bits] = Vec(Bits(32 bits),keyWidth.value / 32)
      when(firstRound) {
        cntStage := 2
      } .otherwise {
        cntStage := cntStage + 1
      }
      newKeyTmp := genRoundKeyBase(lastRoundKey,roundNum,firstRound?U(1)|cntStage,keyMode)
      firstKey := getKey(keyMode,roundNum(0),newKeyTmp)

      // cal next cnt stage for 2nd round function
      nxtCntStage := roundNum(0) ? cntStage | (cntStage + 1)
      newKey := genRoundKeyBase(newKeyTmp.asBits,(roundNum+1),nxtCntStage,keyMode)
      secKey := getKey(keyMode,!roundNum(0),newKey)
      keyStashReg := newKey.asBits
      B(secKey,firstKey)
    }
  }

  val lastRoundKey:Bits = Bits(keyWidth.value bits)
  when(io.startGetKey) {
    when(io.keyMode === AESMODE.AES128.asBits.asUInt) {
      lastRoundKey := io.initKey(127 downto 0).subdivideIn(32 bits).asBits.resize(keyWidth.value bits)
    } otherwise {
      lastRoundKey := io.initKey.subdivideIn(32 bits).asBits
    }
  } otherwise {
    lastRoundKey := keyStashReg
  }

  io.curKey := genRoundKey(io.startGetKey,lastRoundKey,io.roundNum,io.keyMode)

  def driveFrom(startGetKey:Bool,initKey:Bits,roundNum:UInt,keyMode:UInt):Bits = {
    io.initKey := initKey
    io.keyMode := keyMode
    io.startGetKey := startGetKey
    io.roundNum := roundNum
    io.curKey
  }
}
