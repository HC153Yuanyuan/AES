
import AES.AES_CORE
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.SpinalConfig
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core._
import spinal.core.sim._
import spinal.lib._

object AESREF {
  def EncAES(plainString:String,keyString:String):String = {
    import javax.crypto.Cipher
    import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
    assert(Array(128,192,256).contains(keyString.length),message = "keyString Error, only 128,192 and 256 is allowed!")
    assert(plainString.length == 128,message = "plainString Error, only 128 bits plain is allowed!")
    val keyTmp = for(i <- 0 until keyString.length() by 2) yield {
      keyString.substring(i,i+2)
    }
    val plainTmp = for(i <- 0 until plainString.length() by 2) yield {
      plainString.substring(i,i+2)
    }

    val key = keyTmp.toList.map(Integer.parseInt(_, 16).toByte).toArray
    val plain = plainTmp.toList.map(Integer.parseInt(_, 16).toByte).toArray
    val SpecKey = new SecretKeySpec(key,"AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, SpecKey)
    val result = cipher.doFinal(plain).toList.map(String.format("%02x",_)).reduce(_+_).substring(0,32)
    result
  }
}

class AESCoreTester extends AnyFunSuite {
  test("AESCoreStd_128"){
    val flags = VCSFlags(
      compileFlags = List("-kdb"),
      elaborateFlags = List("-kdb")
    )

    SimConfig.withConfig(SpinalConfig(inlineRom = true)).withVCS(flags).withFSDBWave.compile(new AES_CORE(128 bits)).doSim { dut =>
      dut.clockDomain.forkStimulus(50000)

    }
  }
}
