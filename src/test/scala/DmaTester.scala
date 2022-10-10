import DMA.Main.{ahbCfg, dmaCfg}
import DMA.{CmdChannel, DmaCfg, DmaCtrl, DmaNodeInf, NodeType}
import DataStream.MacStreamCompress
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.{SpinalConfig, UInt, assert, _}
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Arbiter, AhbLite3Config, AhbLite3OnChipRam, AhbLite3OnChipRamMultiPort}
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core.sim._
import spinal.lib.{master, slave}


case class DmaWrapper(c:DmaCfg,ahbCfg:AhbLite3Config) extends Component {
  val io = new Bundle {
      val nodeStream = Vec(master(DmaNodeInf(c,NodeType.fullVersion)),2)
  }

  val ahbArb = AhbLite3Arbiter(ahbCfg,2)
  val dma =  for (i <- 0 until 2) yield DmaCtrl(c,ahbCfg)
  val AhbRam = AhbLite3OnChipRam(ahbCfg, byteCount= 1 << ahbCfg.addressWidth)
  val ramInitData = (for (i <- 0 until (1 << ahbCfg.addressWidth)/4) yield B(i,ahbCfg.dataWidth bits)).toSeq
  AhbRam.ram.init(ramInitData)

  for(i <- 0 until 2) {
    io.nodeStream(i) <> dma(i).io.dmaNode
    ahbArb.io.inputs(i) <> dma(i).io.ahbBus
  }
  ahbArb.io.output <> AhbRam.io.ahb

}

case class DmaTester() extends AnyFunSuite {
  test("DmaTester") {
    val flags = VCSFlags(
      compileFlags = List("-kdb"),
      elaborateFlags = List("-kdb")
    )


    val dmaCfg = DmaCfg(slaveNode = 8,addrWidth = 10)
    val ahbCfg = AhbLite3Config(addressWidth = dmaCfg.addrWidth,dataWidth = dmaCfg.dataWidth)


    def setCmd(io:CmdChannel,wrOp:Boolean,startAddr:Int,len:Int) = {
      io.startAddr #= startAddr
      io.wrOp #= wrOp
      io.transLen #= len
      io.transCancel #= false
      io.pri #= 0
      io.reqVld #= true
    }

    def waitCmd(cmd:CmdChannel,clock:ClockDomain) = {
      var waitFlag = true
      while (waitFlag) {
        clock.waitActiveEdge()
        if (cmd.rspStream.valid.toBoolean == true) {
          waitFlag = false
        }
      }
      clock.waitActiveEdge()
      releasecmd(cmd)
    }

    def releasecmd(io:CmdChannel) = {
      io.reqVld #= false
      io.startAddr #= 0
      io.wrOp #= scala.util.Random.nextInt % 2==1
      io.transLen #= 0
      io.transCancel #= false
      io.pri #= 0
    }

    def driveData(dma:DmaNodeInf,clock:ClockDomain) = {
      waitCmd(dma.cmd,clock)
      clock.waitActiveEdge()
      releasecmd(dma.cmd)

      var count1 = 0
      var timeout = 0
      while (count1 < 20) {
        timeout = 0
        dma.wrChannel.wrStream.valid.randomize()
        dma.wrChannel.wrStream.fragment #= count1 + 10
        clock.waitSampling()
        if (dma.wrChannel.wrStream.valid.toBoolean) {
          while (!dma.wrChannel.wrStream.ready.toBoolean) {
            clock.waitSampling()
            timeout = timeout + 1
            if (timeout > 50)
              simFailure()
          }
          count1 += 1
        }
      }
      dma.wrChannel.wrStream.valid #= false
    }

    def ioInit(dut:DmaNodeInf) = {
      dut.cmd.reqVld #= false
      dut.cmd.startAddr #= 0
      dut.cmd.wrOp #= scala.util.Random.nextInt % 2==1
      dut.cmd.transLen #= 0
      dut.cmd.transCancel #= false
      dut.cmd.pri #= 0

      dut.rdChannel.rdStream.ready #= false
      dut.wrChannel.wrStream.valid #= false
      dut.wrChannel.wrStream.fragment #= 0
      dut.wrChannel.wrStream.last #= false
    }


    SimConfig.withConfig(SpinalConfig(inlineRom = true)).withVCS(flags).withWave.compile(DmaWrapper(dmaCfg,ahbCfg)).doSimUntilVoid { dut =>
      dut.clockDomain.forkStimulus(100000)
      dut.clockDomain.waitActiveEdge()
      for (node <- dut.io.nodeStream) {
        ioInit(node)
      }

      val threadPoll = for (i <- 0 until 2) yield fork {
        if (i == 0) {
          setCmd(dut.io.nodeStream(i).cmd,true,0,20)
        } else {
          setCmd(dut.io.nodeStream(i).cmd,true,80,20)
        }
        driveData(dut.io.nodeStream(i),dut.clockDomain)
      }


      fork {
        dut.clockDomain.waitSampling(800)
        simSuccess()
      }

    }
  }

}
