package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Config, AhbLite3Master}
import spinal.lib.com.spi.Apb3SpiMasterCtrl
import spinal.lib.fsm._

case class DmaCtrl(c:DmaCfg,ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    //val getNextCmd = out Bool()
    val dmaNode = master(DmaNodeInf(c,NodeType.fullVersion))
    val ahbBus =  master(AhbLite3(ahbCfg))
  }

  val cmdChkPass = False
  val dmaTransDone = False
  val startAddr = U(0x0, c.addrWidth bits)
  val endAddr = U(0x100, c.addrWidth bits)

  def ChkCmd(node:CmdChannel)  = {
    val result = True
    when( node.startAddr < startAddr || (node.startAddr + node.transLen * c.dataWidth/8) > endAddr )  {
      result := False
    } otherwise {
      result := True
    }
    result
  }

  val fsm = new StateMachine {
    val ST_IDLE :State = new State with EntryPoint{
      whenIsActive {
        when(io.dmaNode.cmd.reqVld) {
          goto(ST_CHK)
        }
      }
    }

    val ST_CHK :State = new State {
      whenIsActive {
        when(cmdChkPass) {
          goto(ST_WORK)
        } otherwise {
          goto(ST_IDLE)
        }
      }
    }

    val ST_WORK: State = new State {
      whenIsActive{
        when(dmaTransDone) {
          goto(ST_IDLE)
        }
      }
    }
  }


  val logic = new Area {
    val addrLatch = RegNextWhen(io.dmaNode.cmd.startAddr,io.dmaNode.cmd.rspVld)
    val transLenLatch = RegNextWhen(io.dmaNode.cmd.transLen,io.dmaNode.cmd.rspVld)
    val writeLatch = RegNextWhen(io.dmaNode.cmd.wrOp,io.dmaNode.cmd.rspVld)
    val transCnt = Reg(UInt(c.lenWidth bits)) init 0
    val burstRemainCnt = Reg(UInt(c.lenWidth bits)) init 0
    val ahbBurstLast = io.ahbBus.last()
    val noBurstTrans = io.ahbBus.HBURST === B"000"
    val busrtFirstTrans = RegInit(False) setWhen(ahbBurstLast || io.dmaNode.cmd.rspVld)  clearWhen(io.ahbBus.fire() && io.ahbBus.HTRANS(1) && ~ahbBurstLast)
    val burstOnGoing = RegInit(False) setWhen(busrtFirstTrans.fall() && !noBurstTrans) clearWhen(ahbBurstLast || io.dmaNode.cmd.rspVld)
    val dataReady = False



    io.ahbBus.HSEL := False
    io.ahbBus.HTRANS := AhbLite3.IDLE
    io.ahbBus.HADDR := 0
    io.ahbBus.HWRITE := False
    io.ahbBus.HREADY := io.ahbBus.HREADYOUT
    io.ahbBus.HBURST := B"000"
    io.ahbBus.HSIZE := B"000"
    io.ahbBus.HMASTLOCK := False
    io.ahbBus.HPROT := B"0000"
    io.ahbBus.HWDATA := 0

    io.dmaNode.wrChannel.wrStream.ready := False
    io.dmaNode.rdChannel.rdStream.valid := False
    io.dmaNode.rdChannel.rdStream.last := False
    io.dmaNode.rdChannel.rdStream.fragment := io.ahbBus.HRDATA




    when(io.dmaNode.cmd.rspVld) {
      burstRemainCnt := io.dmaNode.cmd.transLen
    } elsewhen(ahbBurstLast) {
      burstRemainCnt := transLenLatch - transCnt - 1
    }

    def getBurstCfg(cnt:UInt) = {
      val HBURST    = Bits(3 bits)
      when(cnt >= 8) {
        HBURST := B"101"
      } elsewhen(cnt >= 4) {
        HBURST := B"011"
      } elsewhen(cnt > 1) {
        HBURST := B"001"
      } otherwise {
        HBURST := B"000"
      }
      HBURST
    }

    io.dmaNode.cmd.rspVld := False
    io.dmaNode.cmd.rspCode := U(0,c.rspWidth bits)

    fsm.ST_CHK.whenIsActive {
      cmdChkPass := ChkCmd(io.dmaNode.cmd)
      io.dmaNode.cmd.rspVld := True
      io.dmaNode.cmd.rspCode := cmdChkPass ? U(0,c.rspWidth bits) | U(1,c.rspWidth bits)
      transCnt := 0
    }

    fsm.ST_WORK.whenIsActive {
      dataReady := writeLatch ? io.dmaNode.wrChannel.wrStream.valid | io.dmaNode.rdChannel.rdStream.ready
      dmaTransDone := transCnt === transLenLatch

      when(io.ahbBus.fire() && io.ahbBus.HTRANS(1)) {
        transCnt := transCnt + 1
      }

      io.ahbBus.HSEL := True
      when((busrtFirstTrans || noBurstTrans) && dataReady) {
        io.ahbBus.HTRANS := AhbLite3.NONSEQ
      } .elsewhen(burstOnGoing && dataReady) {
        io.ahbBus.HTRANS := AhbLite3.SEQ
      } .elsewhen(burstOnGoing && !dataReady) {
        io.ahbBus.HTRANS := AhbLite3.BUSY
      } .otherwise {
        io.ahbBus.HTRANS := AhbLite3.IDLE
      }

      io.ahbBus.HADDR := addrLatch + transCnt*4
      io.ahbBus.HWRITE := writeLatch
      io.ahbBus.HREADY := io.ahbBus.HREADYOUT
      io.ahbBus.HBURST := getBurstCfg(burstRemainCnt)
      io.ahbBus.HSIZE := B"010"
      io.ahbBus.HMASTLOCK := False
      io.ahbBus.HPROT := B"0000"
      io.ahbBus.HWDATA := RegNext(writeLatch ? io.dmaNode.wrChannel.wrStream.fragment | 0)


      io.dmaNode.wrChannel.wrStream.ready := io.ahbBus.HREADY && io.ahbBus.HTRANS(1)
      io.dmaNode.rdChannel.rdStream.valid := io.ahbBus.fire() && io.ahbBus.HTRANS(1)
      io.dmaNode.rdChannel.rdStream.last := transCnt === transLenLatch - 1
      io.dmaNode.rdChannel.rdStream.fragment := io.ahbBus.HRDATA
    }


  }


}
