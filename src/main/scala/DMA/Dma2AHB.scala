package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config}
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
case class Dma2AHB(c:DmaCfg,ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val ahbAllowMaxBurst = in UInt(5 bits)
    val busIdle = out Bool()
    val dmaNode = slave(DmaNodeInf(c,NodeType.fullVersion,withSlaveId = true))
    val ahbBus =  master(AhbLite3(ahbCfg))
  }

  val dmaTransDone = False

  val fsm = new StateMachine {
    val ST_IDLE: State = new State with EntryPoint {
      whenIsActive {
        when(io.dmaNode.cmd.cmdStream.fire) {
          goto(ST_WORK)
        }
      }
    }

    val ST_WORK: State = new State {
      whenIsActive {
        when(dmaTransDone) {
          goto(ST_IDLE)
        }
      }
    }
  }

  val fsmInWorkState = fsm.isActive(fsm.ST_WORK)

  val BusLogic = new Area {
    val DmaWatchDog = Reg(UInt(8 bits)) init 0
    val DmaHung = RegInit(False)
    val cmdLatch = RegNextWhen(io.dmaNode.cmd.cmdStream.payload,io.dmaNode.cmd.cmdStream.fire)
    val wrOp = io.dmaNode.cmd.cmdStream.fire ? io.dmaNode.cmd.cmdStream.wrOp | cmdLatch.wrOp
    val transLen = io.dmaNode.cmd.cmdStream.fire ? io.dmaNode.cmd.cmdStream.transLen | cmdLatch.transLen
    val startAddr = io.dmaNode.cmd.cmdStream.fire ? io.dmaNode.cmd.cmdStream.startAddr | cmdLatch.startAddr
    val transCnt = Reg(UInt(c.lenWidth bits)) init 0
    val ahbBeatCounter = Reg(UInt(5 bits)) init 0
    val swConfigMaxBurst = RegNextWhen(io.ahbAllowMaxBurst,io.ahbBus.HTRANS === AhbLite3.IDLE)
    val fifoAva = wrOp ? io.dmaNode.wrChannel.wrStream.valid | io.dmaNode.rdChannel.rdStream.ready
    val burstStartAddr = startAddr + transCnt * 4
    val lastWord = transCnt >= (transLen -1)
    val lastCapture = RegInit(False)
    val transCntReach = (transCnt >= transLen) || lastCapture || DmaHung
    val ahbBurstQuit = (burstStartAddr(9 downto 2) === 0xff) || (ahbBeatCounter >= swConfigMaxBurst - 1) || !fifoAva || lastWord || DmaHung
    val ahbCanTrans = fifoAva && fsmInWorkState && !transCntReach && !DmaHung







    when(DmaWatchDog === 0xff) {
      DmaHung := True
      assert(False,"DMA HUNG")
    } elsewhen(fsm.isActive(fsm.ST_IDLE)) {
      DmaHung := False
    }

    val AHBST = new StateMachine{
      val ST_IDLE:State = new State with EntryPoint{
        whenIsActive{
          when(fsmInWorkState && !transCntReach) {
            goto(ST_ADDR)
          }
        }
      }

      val ST_ADDR:State = new State{
        whenIsActive {
          when(io.ahbBus.fire()) {
            when(!fifoAva) {
              goto(ST_BUSY)
            } elsewhen(ahbBurstQuit) {
              goto(ST_DATA)
            } otherwise {
              goto(ST_DATA_ADDR)
            }
          }
        }
      }

      val ST_DATA_ADDR:State = new State {
        whenIsActive{
          when(io.ahbBus.fire()) {
            when(!fifoAva) {
              goto(ST_BUSY)
            } elsewhen(ahbBurstQuit) {
              goto(ST_DATA)
            } otherwise {
              goto(ST_DATA_ADDR)
            }
          }
        }
      }

      val ST_DATA:State = new State{
        whenIsActive{
          when(io.ahbBus.fire()) {
            when(ahbCanTrans) {
              goto(ST_ADDR)
            } otherwise {
              goto(ST_IDLE)
            }
          }
        }
      }

      val ST_BUSY:State = new State{
        whenIsActive{
          when(fifoAva || DmaHung) {
            when(ahbBurstQuit) {
              goto(ST_DATA)
            } otherwise {
              goto(ST_DATA_ADDR)
            }
          }
        }
      }
    }

    when(AHBST.isActive(AHBST.ST_BUSY)) {
      DmaWatchDog := DmaWatchDog + 1
    } otherwise {
      DmaWatchDog := 0
    }

    when(fsm.isActive(fsm.ST_IDLE)) {
      transCnt := 0
      lastCapture := False
    } otherwise {
      when(io.ahbBus.fire() && (AHBST.isActive(AHBST.ST_ADDR) || AHBST.isActive(AHBST.ST_DATA_ADDR))) {
        transCnt := transCnt + 1
      }
      when(wrOp) {
        lastCapture := lastCapture | (io.dmaNode.wrChannel.wrStream.last && io.dmaNode.wrChannel.wrStream.fire)
      }
    }

    when(io.ahbBus.HTRANS === AhbLite3.IDLE) {
      ahbBeatCounter := 0
    } elsewhen(io.ahbBus.fire() && io.ahbBus.HTRANS(1)) {
      ahbBeatCounter := ahbBeatCounter + 1
    }

    when(fsm.isActive((fsm.ST_WORK))) {
      dmaTransDone := wrOp ? (transCntReach && io.ahbBus.fire() && io.ahbBus.HTRANS=/=AhbLite3.BUSY) | (transCntReach && AHBST.isActive(AHBST.ST_IDLE))
    }




    io.ahbBus.HSEL := fsm.isActive(fsm.ST_WORK)
    io.ahbBus.HTRANS := AHBST.isActive(AHBST.ST_ADDR) ? B"10"|
                        ( AHBST.isActive(AHBST.ST_DATA_ADDR) ? B"11" |
                        ( AHBST.isActive(AHBST.ST_BUSY) ? B"01" | B"00"))

    io.ahbBus.HADDR := burstStartAddr
    io.ahbBus.HWRITE := wrOp
    io.ahbBus.HREADY := io.ahbBus.HREADYOUT
    io.ahbBus.HBURST := (AHBST.isActive(AHBST.ST_ADDR) || AHBST.isActive(AHBST.ST_DATA_ADDR) ||  AHBST.isActive(AHBST.ST_BUSY)) ? B"001" | B"000"
    io.ahbBus.HSIZE := B"010"
    io.ahbBus.HMASTLOCK := False
    io.ahbBus.HPROT := B"0000"
    io.ahbBus.HWDATA := wrOp ? (io.dmaNode.wrChannel.wrStream.fire ? io.dmaNode.wrChannel.wrStream.fragment(31 downto 0) | RegNext(io.ahbBus.HWDATA) ) | 0


    io.dmaNode.wrChannel.wrStream.ready := wrOp && io.ahbBus.fire() && ( AHBST.isActive(AHBST.ST_DATA_ADDR) ||  AHBST.isActive(AHBST.ST_DATA) )

    io.dmaNode.rdChannel.rdStream.valid := !wrOp && io.ahbBus.fire() && ( AHBST.isActive(AHBST.ST_DATA_ADDR) ||  AHBST.isActive(AHBST.ST_DATA) )
    io.dmaNode.rdChannel.rdStream.last := transCntReach
    io.dmaNode.rdChannel.rdStream.fragment := cmdLatch.id ## io.ahbBus.HRDATA

    io.dmaNode.cmd.cmdStream.ready := fsm.isActive(fsm.ST_IDLE)

    io.dmaNode.rsp.rspStream.valid := dmaTransDone
    io.dmaNode.rsp.rspStream.payload.rspCode := 0
    io.dmaNode.rsp.rspStream.payload.id := cmdLatch.id


    io.busIdle := fsm.isActive(fsm.ST_IDLE) && AHBST.isActive(AHBST.ST_IDLE)

  }

}
