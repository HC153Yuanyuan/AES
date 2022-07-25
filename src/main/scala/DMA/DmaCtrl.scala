package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.{AhbLite3Config, AhbLite3Master}
import spinal.lib.com.spi.Apb3SpiMasterCtrl
import spinal.lib.fsm._

case class DmaCtrl(c:DmaCfg,ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
    val getNextCmd = out Bool()
    val dmaNode = slave(DmaNodeInf(c,NodeType.fullVersion))
    val ahbBus =  master(AhbLite3Master(ahbCfg))
  }

  val fsm = new StateMachine {
    val cmdChkPass = False
    val dmaTransDone = False
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



}
