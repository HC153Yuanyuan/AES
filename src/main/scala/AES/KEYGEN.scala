package AES

import spinal.core._
import spinal.lib._

case class KEYGEN(keyWidth:BitCount) extends Component {
  val io = new Bundle {
    val initKey = in Bits(keyWidth.value bits)
    val keyMode = in UInt(2 bits)
    val startGetKey = in Bool()
    val roundNum = in UInt(4 bits)
    val curKey = out (if(AES.enable2Round) Bits(256 bits) else Bits(128 bits))
  }

  val cntStage = Reg(UInt(4 bits))

  val keyStashReg = Reg(Bits(keyWidth.value bits))

  val keyRcon = Mem(Bits(8 bits), AES_CONFIG.rcon(keyWidth).map(B(_, 8 bits)))

  def genRoundKeyBase(firstRound:Bool,lastRoundKey:Bits,roundNum:UInt,cntStage:UInt,keyMode:UInt):Vec[Bits] = {
    val newKey = Vec(Bits(32 bits),keyWidth.value / 32)
    val lastKey = lastRoundKey.subdivideIn(32 bits)

    report(Seq(firstRound,"\t",lastRoundKey,"\t",roundNum,"\t",cntStage))

    if (keyWidth.value == 256) {
      newKey := lastKey
      when(keyMode === AESMode.AES_256) {

        when(roundNum(0) === True) {
          newKey(0) := lastKey(0) ^ AES_CONFIG.gFunc(Rkey_in=lastKey(7),keyRcon(firstRound?U(1)|cntStage))
          newKey(1) := lastKey(1) ^ newKey(0)
          newKey(2) := lastKey(2) ^ newKey(1)
          newKey(3) := lastKey(3) ^ newKey(2)
        } otherwise {
          newKey(4) := lastKey(4) ^ AES_CONFIG.hFunc(Rkey_in=lastKey(3))
          newKey(5) := lastKey(5) ^ newKey(4)
          newKey(6) := lastKey(6) ^ newKey(5)
          newKey(7) := lastKey(7) ^ newKey(6)
        }
      } otherwise {
        newKey(0) := lastKey(0) ^ AES_CONFIG.gFunc(Rkey_in=lastKey(3),keyRcon(roundNum))
        newKey(1) := lastKey(1) ^ newKey(0)
        newKey(2) := lastKey(2) ^ newKey(1)
        newKey(3) := lastKey(3) ^ newKey(2)
      }
      newKey
    } else if(keyWidth.value == 128) {
      newKey(0) := lastKey(0) ^ AES_CONFIG.gFunc(Rkey_in=lastKey(3),keyRcon(roundNum))
      newKey(1) := lastKey(1) ^ newKey(0)
      newKey(2) := lastKey(2) ^ newKey(1)
      newKey(3) := lastKey(3) ^ newKey(2)
      newKey
    } else {
      newKey.foreach(_ := 0)
      newKey
    }
  }


  def getKey(keyMode:UInt,roundNum_0:Bool,newKey:Vec[Bits]):Bits = {
    val returnKey = Bits(128 bits)
    if(keyWidth.value == 256) {
      when(keyMode === AESMode.AES_256) {
        returnKey := roundNum_0.mux(
          True  -> newKey.reverse.asBits()(127 downto 0),
          False  -> newKey.reverse.asBits()(255 downto 128)
        )
      } otherwise {
        returnKey := newKey.slice(0,4).reverse.asBits
      }
      returnKey
    } else {
      returnKey := newKey.slice(0,4).reverse.asBits
      returnKey
    }
  }

  def genRoundKey(firstRound:Bool,lastRoundKey:Bits,roundNum:UInt,keyMode:UInt) = {
    val newKey:Vec[Bits] = Vec(Bits(32 bits), keyWidth.value/32)
    val nxtCntStage = UInt(4 bits)



    if (!AES.enable2Round) {
      val returnKey = Bits(128 bits)
      when(firstRound) {
        nxtCntStage := 1
      } .elsewhen((roundNum(0) === False) ) {
        nxtCntStage := cntStage + 1
      } otherwise {
        nxtCntStage := cntStage
      }

      newKey := genRoundKeyBase(firstRound,lastRoundKey,roundNum,cntStage,keyMode)
      cntStage := nxtCntStage
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
      newKeyTmp := genRoundKeyBase(firstRound,lastRoundKey,roundNum,cntStage,keyMode)

      firstKey := getKey(keyMode,roundNum(0),newKeyTmp)

      nxtCntStage := roundNum(0) ? cntStage | (cntStage + 1)
      newKey := genRoundKeyBase(False,newKeyTmp.asBits,(roundNum+1),nxtCntStage,keyMode)
      secKey := getKey(keyMode,!roundNum(0),newKey)
      keyStashReg := newKey.asBits
      //report(Seq(secKey,firstKey))
      B(secKey,firstKey)
    }
  }

  val lastRoundKey:Bits = Bits(keyWidth.value bits)
  when(io.startGetKey) {
    when(io.keyMode === 0) {
      lastRoundKey := io.initKey(127 downto 0).subdivideIn(32 bits).reverse.asBits.resize(keyWidth.value bits)
    } otherwise {
      lastRoundKey := io.initKey.subdivideIn(32 bits).reverse.asBits
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