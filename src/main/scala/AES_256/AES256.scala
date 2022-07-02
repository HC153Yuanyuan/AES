package AES_256

import spinal.core._
import spinal.lib._
case class AES256() extends Component {
  val gIO  = CryptoBlockConfig(
    keyWidth   = AES_CONFIG.keyWidth,
    blockWidth = AES_CONFIG.blockWidth
  )

  val io = slave(CryptoBlockIO(gIO))
  val engine      = new AES_ENGINE(AES_CONFIG.keyWidth)
  engine.io.engine      <> io
}


case class AES_ENGINE(keyWidth:BitCount) extends Component {
  assert(Array(128,256).contains(keyWidth.value),"keyWidth Error, only 128 and 256 is allowed!")
  val cfg  = CryptoBlockConfig(
    keyWidth   = keyWidth,
    blockWidth = AES_CONFIG.blockWidth
  )

  val io = new Bundle{
    val engine = slave(CryptoBlockIO(cfg))
  }

  val rndFuc = AESRoundFunc(AES_CONFIG.enable2Round)
  val keyOp = AESKeyGen(keyWidth)

  val encDecSel = RegNextWhen(io.engine.cmd.fire,io.engine.cmd.enc,False)
  val lastRoundCnt = Reg(UInt(4 bits))



  val totalRoundNum =  io.engine.cmd.mode.mux(
    0 -> U(10,4 bits),
    1 -> U(14,4 bits),
    default->U(10,4 bits)
  )

  when(io.engine.cmd.fire) {
    when(io.engine.cmd.enc) {
      if(AES_CONFIG.enable2Round) {
        lastRoundCnt := totalRoundNum - 3
      } else {
        lastRoundCnt := totalRoundNum - 1
      }
    } otherwise {
      lastRoundCnt := 0
    }
  }




  val CtrlState = new Area {
    val enginRdy = RegInit(True)
    val ongoing = RegInit(False)
    val rndCnt = Reg(UInt(4 bits)) init  0
    val dataBuf = Reg(Bits(128 bits)) init  0
    val rndStep = if(AES_CONFIG.enable2Round) 2 else 1
    val lastRound = (rndCnt === lastRoundCnt)
    val currndKey = if(AES_CONFIG.enable2Round)  Bits(256 bits)  else Bits(128 bits)
    val nextRndCnt = UInt(rndCnt.getWidth bits)
    val keyModeLatch = RegNextWhen(io.engine.cmd.mode,io.engine.cmd.fire)
    val firstRoundKey = Bits(128 bits)

    firstRoundKey := io.engine.cmd.key(127 downto 0)

    // set default value
    currndKey := 0
    keyOp.driveFrom(False,0,0,0)
    rndFuc.driveFrom( 0, 0,True)

    //    report(Seq("encDecSel = ",encDecSel))
    //    report(Seq("lastRoundCnt = ",lastRoundCnt))
    //    report(Seq("rndCnt = ",rndCnt))

    report(Seq("rndCnt = ",rndCnt))
    report(Seq("currndKey = ",currndKey))
    report(Seq("dataBuf = ",dataBuf))


    nextRndCnt := rndCnt

    when(io.engine.cmd.fire) {
      ongoing := True
      enginRdy := False
      when(io.engine.cmd.enc) {
        nextRndCnt := 1
      } otherwise {
        nextRndCnt := totalRoundNum-1
      }
      rndCnt := nextRndCnt
      currndKey := keyOp.driveFrom(True,io.engine.cmd.key, nextRndCnt, io.engine.cmd.mode)
      dataBuf := rndFuc.driveFrom(io.engine.cmd.block ^ firstRoundKey , currndKey, True)
    } .elsewhen(ongoing) {
      enginRdy := False
      when(encDecSel) {
        nextRndCnt := rndCnt + rndStep
      } otherwise {
        nextRndCnt := rndCnt - rndStep
      }

      currndKey := keyOp.driveFrom(False,io.engine.cmd.key,nextRndCnt,keyModeLatch)
      dataBuf := rndFuc.driveFrom( dataBuf, currndKey,!lastRound)
      when(!lastRound) {
        rndCnt := nextRndCnt
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
  io.engine.rsp.block := CtrlState.dataBuf
  io.engine.rsp.valid := RegNext(CtrlState.lastRound && CtrlState.ongoing,False)
}

