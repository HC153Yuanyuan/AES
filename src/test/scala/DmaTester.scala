
import DMA._
import DataStream.MacStreamCompress
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.{SpinalConfig, UInt, assert, _}
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.lib.bus.amba3.ahblite.{AhbLite3, AhbLite3Arbiter, AhbLite3Config, AhbLite3OnChipRam, AhbLite3OnChipRamMultiPort}
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core.sim._
import spinal.lib.bus.bram.BRAM
import spinal.lib.{master, slave}

import scala.collection.mutable.ArrayBuffer

object DmaDriver{

  def setCmd(io:CmdChannel,wrOp:Boolean,startAddr:Int,len:Int,clock:ClockDomain) = {
    var waitFlag = true
    io.cmdStream.startAddr #= startAddr
    io.cmdStream.wrOp #= wrOp
    io.cmdStream.transLen #= len
    io.cmdStream.pri #= 0
    io.cmdStream.valid #= true
    while (waitFlag) {
      clock.waitActiveEdge()
      if (io.cmdStream.valid.toBoolean && io.cmdStream.ready.toBoolean == true) {
        waitFlag = false
      }
    }
    io.cmdStream.valid #= false
    clock.waitActiveEdge()
  }


  def driveWrData(dma:DmaNodeInf,data:List[Int],clock:ClockDomain,withLast:Boolean = true,abortCase:Boolean = false) = {
    var timeout = 0
    var count = 0
    while(count < data.size) {
      timeout = 0
      dma.wrChannel.wrStream.valid.randomize()
      dma.wrChannel.wrStream.fragment #= data(count)
      if(withLast) {
        dma.wrChannel.wrStream.last #=  count == (data.size-1)
      }

      clock.waitSampling()
      if (dma.wrChannel.wrStream.valid.toBoolean) {
        while (!dma.wrChannel.wrStream.ready.toBoolean) {
          clock.waitSampling()
          timeout = timeout + 1
          if (timeout > 500)
            simFailure("wr wait timeout!")
        }
        count = count + 1
      }
    }
    dma.wrChannel.wrStream.valid #= false
    dma.wrChannel.wrStream.last #= false

    val timeoutThreshold = if(abortCase)  5000 else 500

    timeout = 0
    while (!dma.rsp.rspStream.valid.toBoolean) {
      clock.waitSampling()
      timeout = timeout + 1
      if (timeout > timeoutThreshold)
        simFailure("wait rspStream valid timeout!")
    }
  }

  def driveRdData(dma:DmaNodeInf,len:Int,clock:ClockDomain):List[Long] = {
    val data = ArrayBuffer[Long]()
    var timeout = 0
    var count = 0
    while(count < len) {
      timeout = 0
      dma.rdChannel.rdStream.ready.randomize()
      clock.waitSampling()
      if (dma.rdChannel.rdStream.ready.toBoolean) {
        while (!dma.rdChannel.rdStream.valid.toBoolean) {
          clock.waitSampling()
          timeout = timeout + 1
          if (timeout > 5000)
            simFailure("RD wait timeout!")
        }
        data.append(dma.rdChannel.rdStream.payload.fragment.toLong)
        if((count == (len - 1)) ^ dma.rdChannel.rdStream.last.toBoolean) {
          simFailure("RD last flag check fail!")
        }
        count = count + 1
      }
    }
    dma.rdChannel.rdStream.ready #= false
    data.toList
  }


  def readTestData(ram:BRAM,startAddr:Long,len:Int,clock:ClockDomain):List[Long] = {
    val data = ArrayBuffer[Long]()
    clock.waitSampling(1)
    for (rdCnt <- 0 until len) {
      ram.en #= true
      ram.we #= 0
      ram.addr #= startAddr + rdCnt*4
      ram.wrdata #= 0
      clock.waitSampling(1)
      data.append(ram.rddata.toLong)
    }
    ram.en #= false
    data.toList
  }

  def writeTestData(ram:BRAM,startAddr:Long,data:List[Int],clock:ClockDomain) = {
    clock.waitActiveEdge()
    for (i <- 0 until data.size) {
      ram.en #= true
      ram.we #= 0xf
      ram.addr #= startAddr + i*4
      ram.wrdata #= data(i)
      clock.waitSampling()
    }
    ram.en #= false
  }

  def writeOp(dut:DmaNodeInf,ram: BRAM, startAddr:Long,data:List[Int],nodeClock:ClockDomain,busClock:ClockDomain) = {
    setCmd(dut.cmd,true,startAddr.toInt,data.size,nodeClock)
    driveWrData(dut, data, nodeClock);
    val actualData = readTestData(ram,startAddr, data.size,busClock)
    if(actualData != data) {
      simFailure("wr data check fail: " + "ref is " + data + "\nactual is: " + actualData)
    }
  }

  def readOp(dut:DmaNodeInf,ram: BRAM, startAddr:Long,data:List[Int],nodeClock:ClockDomain,busClock:ClockDomain) = {
    writeTestData(ram,startAddr, data, busClock)
    setCmd(dut.cmd,false,startAddr.toInt,data.size,nodeClock)
    val actualData = driveRdData(dut,data.size,nodeClock)
    if(actualData != data) {
      simFailure("rd data check fail: " + "ref is " + data + "\nactual is: " + actualData)
    }
  }

  def writeAbort(dut:DmaNodeInf,ram: BRAM, startAddr:Long,data:List[Int],nodeClock:ClockDomain,busClock:ClockDomain) = {
    setCmd(dut.cmd,true,startAddr.toInt,data.size,nodeClock)
    val actualDataSize = util.Random.nextInt(data.size + 1)
    driveWrData(dut, data.slice(0,actualDataSize), nodeClock)
  }
}

object DmaTester {


    val dmaCfg = DmaCfg(slaveNode = 4,addrWidth = 10)
    val ahbCfg = AhbLite3Config(addressWidth = dmaCfg.addrWidth,dataWidth = dmaCfg.dataWidth)


    def ioInit(inf:DmaNodeInf) = {
      inf.cmd.cmdStream.valid #= false
      inf.rsp.rspStream.ready #= true

      inf.rdChannel.rdStream.ready #= false
      inf.wrChannel.wrStream.valid #= false
    }


  def main(args:Array[String]) {

      val flags = VCSFlags(
        compileFlags = List("-kdb"),
        elaborateFlags = List("-kdb")
      )

      SimConfig.withConfig(SpinalConfig(inlineRom = true)).withWave.doSimUntilVoid(new TestWrapper(dmaCfg,ahbCfg),"xxx",seed = 74779957) { dut =>
      val busDomain = ClockDomain(
        clock = dut.io.sysBusClk,
        reset = dut.io.sysBusRst,
        config = ClockDomainConfig(
          clockEdge = RISING,
          resetKind = ASYNC,
          resetActiveLevel = LOW)
      )
      val nodeDomain = ClockDomain(
        dut.io.nodeClk,
        dut.io.nodeRst,
        config = ClockDomainConfig(
          clockEdge = RISING,
          resetKind = ASYNC,
          resetActiveLevel = LOW)
      )

      nodeDomain.forkStimulus(12500 )
      busDomain.forkStimulus(25000)

      for (node <- dut.io.nodeStream) {
        ioInit(node)
      }
      for (node <- dut.io.testStream) {
        node.en #= false
      }
      dut.io.dmaEnable #= false


      nodeDomain.waitActiveEdge()
      busDomain.waitActiveEdge()

      nodeDomain.waitSampling(10)
      dut.io.dmaEnable #= true


      val threadPoll = for (i <- 0 until dmaCfg.slaveNode) yield fork {
        val wrData = for (start <- 0 until 20) yield {start+i*20}
        val rdData = for (start <- 0 until 20) yield {start+i*20 + 100}
        DmaDriver.writeOp(dut.io.nodeStream(i),dut.io.testStream(i),i*80,wrData.toList,nodeDomain,busDomain)
        DmaDriver.readOp(dut.io.nodeStream(i),dut.io.testStream(i),i*80,rdData.toList,nodeDomain,busDomain)
        DmaDriver.writeOp(dut.io.nodeStream(i),dut.io.testStream(i),i*80,wrData.toList,nodeDomain,busDomain)
        DmaDriver.readOp(dut.io.nodeStream(i),dut.io.testStream(i),i*80,rdData.toList,nodeDomain,busDomain)
      }

        fork {
          while(true) {
            dut.busArea.AhbRam.forceReadyDown.randomize()
            busDomain.waitSampling(

            )
          }

        }

      fork {
        nodeDomain.waitSampling(400000)
        simSuccess()
      }

    }
  }

}
