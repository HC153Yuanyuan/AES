import java.io.PrintWriter

import DataStream._
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.{SpinalConfig, assert, _}
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core.sim._

case class MacStreamCompressTester() extends AnyFunSuite {
  test("MacStreamCompressTester") {
    val flags = VCSFlags(
      compileFlags = List("-kdb"),
      elaborateFlags = List("-kdb")
    )

    def cntToBitmap(in:Int) = if (in==1) {
      0x1
    } else if (in==2) {
      0x3
    } else if (in==3) {
      0x7
    } else {
      0xf
    }

    def genData(lastCnt:Int,curNum:Int):Long = {
      if (curNum == 0) {
        scala.util.Random.nextInt(1000)
      } else if (curNum == 1) {
        (lastCnt + scala.util.Random.nextInt(1000) *  scala.math.pow(2,8)).toLong
      } else if (curNum == 2) {
        (lastCnt + (lastCnt + 1) *  scala.math.pow(2,8) + scala.util.Random.nextInt(1000) *  scala.math.pow(2,16)).toLong
      } else if (curNum == 3) {
        (lastCnt + (lastCnt + 1) *  scala.math.pow(2,8)  + (lastCnt + 2) *  scala.math.pow(2,16) + scala.util.Random.nextInt(100) *  scala.math.pow(2,24)).toLong
      } else {
        (lastCnt + (lastCnt + 1) *  scala.math.pow(2,8)  + (lastCnt + 2) *  scala.math.pow(2,16) + (lastCnt + 3) *  scala.math.pow(2,24)).toLong
      }
    }


    SimConfig.withConfig(SpinalConfig(inlineRom = true)).withVCS(flags).withFSDBWave.compile(new MacStreamCompress()).doSimUntilVoid { dut =>
      dut.clockDomain.forkStimulus(100)
      dut.clockDomain.waitActiveEdge()
      dut.io.lastTrans #= false
      dut.io.flush #= false
      val gensrc = fork {
        var count1 = 0
        val lenInWord = 10 + scala.util.Random.nextInt(60)
        var byteCnt = 0
        var byteEna = scala.util.Random.nextInt(3) + 1
        while (count1 < lenInWord) {
          dut.io.payload_in.valid.randomize()
          dut.io.payload_in.payload.data #= genData(byteCnt,byteEna)
          dut.io.payload_in.payload.byteEna #= cntToBitmap(byteEna)
          dut.clockDomain.waitSampling()
          if (dut.io.payload_in.valid.toBoolean) {
            byteCnt = byteCnt + byteEna
            byteEna = scala.util.Random.nextInt(3) + 1
            count1 += 1
          }
        }
        dut.io.payload_in.valid #= false
        dut.io.lastTrans #= true
        dut.clockDomain.waitSampling()
        dut.io.lastTrans #= false
        dut.clockDomain.waitSampling()
      }


      val getresult = fork {

        val wrt = new PrintWriter("/home/zyy/xxx.txt") //new PrintWriter(new File("/home/zyy/hw_pool.txt"))
        for(i <- 0 until 1000) {
          dut.clockDomain.waitSampling()
          if(dut.io.payload_out.valid.toBoolean) {
            wrt.println("data:\t%x".format(dut.io.payload_out.payload.data.toBigInt.intValue()))
            wrt.println("byteEna:\t%x\n".format(dut.io.payload_out.payload.byteEna.toBigInt.intValue()))
          }
        }
        wrt.close()
        simSuccess()
      }

    }

  }
}
