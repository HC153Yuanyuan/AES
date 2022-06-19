package AES

import spinal.core._
import spinal.lib._

case class AES_CORE(keyWidth:BitCount) extends Component {
  val gIO  = CryptoBlockConfig(
    keyWidth   = keyWidth,
    blockWidth = AES.blockWidth,
    useEncDec  = true
  )

  val io = slave(CryptoBlockIO(gIO))

  val engine      = new AES_ENGINE(keyWidth)

  engine.io.engine      <> io
}

case class AES_ENGINE(keyWidth:BitCount) extends Component {
  assert(Array(128,192,256).contains(keyWidth.value),"keyWidth Error, only 128, 192 and 256 is allowed!")
  val cfg  = CryptoBlockConfig(
    keyWidth   = keyWidth,
    blockWidth = AES.blockWidth,
    useEncDec  = true
  )

  val io = new Bundle{
    val engine = slave(CryptoBlockIO(cfg))
  }

  val rndFuc = MyroundFunction()

  def genRoundKey(lastRoundKey:Bits,roundNum:Bits):Bits = {
    val newKey = Vec(Bits(32 bits),4)
    val lastKey = lastRoundKey.subdivideIn(32 bits)
    newKey(0) := lastKey(0) ^ AES_CONFIG.RconKey(Rkey_in=lastKey(3),roundNum)
    newKey(1) := lastKey(1) ^ newKey(0)
    newKey(2) := lastKey(2) ^ newKey(1)
    newKey(3) := lastKey(3) ^ newKey(2)
    return newKey.reverse.asBits
  }

  val CtrlState = new Area {
    val enginRdy = RegInit(True)
    val ongoing = RegInit(False)
    val nextrndKey = Reg(Bits(128 bits)) init  0
    val rndCnt = Reg(UInt(4 bits)) init  0
    val dataBuf = Reg(Bits(128 bits)) init  0
    val lastRound = rndCnt === (AES_CONFIG.nbrRound(keyWidth)-1)
    val currndKey = Bits(128 bits)
    val keyRcon = Mem(Bits(8 bits), AES_CONFIG.rcon(keyWidth).map(B(_, 8 bits)))

    when(io.engine.cmd.fire) {
      rndFuc.io.roundData := io.engine.cmd.block ^ currndKey
     // rndFuc.io.roundKey := currndKey
      rndFuc.io.needMix := !lastRound
    } .otherwise {
      rndFuc.io.roundData := dataBuf ^ currndKey
     // rndFuc.io.roundKey := currndKey
      rndFuc.io.needMix := !lastRound
    }


    currndKey := 0
    when(io.engine.cmd.fire) {
      ongoing := True
      enginRdy := False
      rndCnt := 1
      currndKey := io.engine.cmd.key //
      nextrndKey := genRoundKey(io.engine.cmd.key.subdivideIn(32 bits).reverse.asBits,B(1,8 bits))
      dataBuf := rndFuc.io.result //Round.roundFunction(io.engine.cmd.block,currndKey,lastRound)
    } .elsewhen(ongoing) {
      enginRdy := False
      currndKey := nextrndKey
      nextrndKey := genRoundKey(nextrndKey.subdivideIn(32 bits).reverse.asBits,keyRcon(rndCnt+1))                 //(B(1,8 bits)<<rndCnt).resize(8 bits))
      dataBuf := rndFuc.io.result //Round.roundFunction(dataBuf,currndKey,lastRound)
      when(!lastRound) {
        rndCnt := rndCnt + 1
        ongoing := True
      } .otherwise {
        rndCnt := 0
        ongoing := False
      }
    } .otherwise {
      enginRdy := True
    }
  }

  io.engine.cmd.ready := CtrlState.enginRdy
  io.engine.rsp.block := CtrlState.dataBuf ^ CtrlState.nextrndKey
  io.engine.rsp.valid := RegNext(CtrlState.lastRound,False)
}
