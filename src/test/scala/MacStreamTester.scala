import java.io.PrintWriter

import DataStream.MacStreamInsert
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.{SpinalConfig, assert, _}
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core.sim._

case class MacStreamTester() extends AnyFunSuite {
  test("MacStreamTester") {
    val flags = VCSFlags(
      compileFlags = List("-kdb"),
      elaborateFlags = List("-kdb")
    )

    val len = scala.util.Random.nextInt(60) + 10
    val targetByte = scala.util.Random.nextInt(len)
    val data = for(i <- 0 until len) yield i
    val lenInWord = scala.math.ceil(len / 4.0).toInt
    val wordData = for(i <- 0 until lenInWord) yield {
      data(i*4) + data((i*4+1)%len) * scala.math.pow(2,8).toLong + data((i*4+2)%len)* scala.math.pow(2,16).toLong + data((i*4+3)%len) * scala.math.pow(2,24).toLong
    }

    val byteEn = for(i <- 0 until lenInWord) yield {
      if (i == lenInWord-1) {
        val tmp = len - 4*(lenInWord-1)
        if (tmp==1) {
          0x1
        } else if (tmp==2) {
          0x3
        } else if (tmp==3) {
          0x7
        } else {
          0xf
        }
      } else {
        0xf
      }
    }



    SimConfig.withConfig(SpinalConfig(inlineRom = true)).withVCS(flags).withFSDBWave.compile(new MacStreamInsert()).doSimUntilVoid { dut =>
      dut.clockDomain.forkStimulus(100)
      dut.clockDomain.waitActiveEdge()

      dut.io.flush #= false
      dut.io.lastTrans #= false
      dut.io.payload_in.valid #= false
      dut.io.payload_out.ready #= false
      dut.io.insertByte #= targetByte
      dut.io.insertEnable #= true
      val gensrc = fork {
        var count1 = 0
        while (count1 < lenInWord) {
          dut.io.payload_in.valid.randomize()
          dut.io.payload_in.payload.data #= wordData(count1)
          dut.io.payload_in.payload.byteEna #= byteEn(count1)
          dut.clockDomain.waitSampling()
          if (dut.io.payload_in.valid.toBoolean && dut.io.payload_in.ready.toBoolean) {
            count1 += 1
          }
        }
        dut.io.payload_in.valid #= false
        dut.clockDomain.waitSampling()
        dut.io.lastTrans #= true
        dut.clockDomain.waitSampling()
        dut.io.lastTrans #= false
        dut.clockDomain.waitSampling()
      }


      val getresult = fork {

        val wrt = new PrintWriter("/home/zyy/xxx.txt") //new PrintWriter(new File("/home/zyy/hw_pool.txt"))
        for(i <- 0 until 1000) {
          dut.io.payload_out.ready #= true
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
