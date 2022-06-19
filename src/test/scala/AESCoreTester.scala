
import AES.{AES_CORE, CryptoBlockIO}
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import org.scalatest.funsuite.AnyFunSuite
import spinal.core.{SpinalConfig, assert, _}
import spinal.core.sim.{SimConfig, VCSFlags}
import spinal.core.sim._
import spinal.lib._

import scala.util.Random

object AESREF {
//  def EncAES(plainString:String,keyString:String):String = {
//    import javax.crypto.Cipher
//    import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
//    assert(Array(128,192,256).contains(keyString.length),message = "keyString Error, only 128,192 and 256 is allowed!")
//    assert(plainString.length == 128,message = "plainString Error, only 128 bits plain is allowed!")
//    val keyTmp = for(i <- 0 until keyString.length() by 2) yield {
//      keyString.substring(i,i+2)
//    }
//    val plainTmp = for(i <- 0 until plainString.length() by 2) yield {
//      plainString.substring(i,i+2)
//    }
//
//    val key = keyTmp.toList.map(Integer.parseInt(_, 16).toByte).toArray
//    val plain = plainTmp.toList.map(Integer.parseInt(_, 16).toByte).toArray
//    val SpecKey = new SecretKeySpec(key,"AES")
//    val cipher = Cipher.getInstance("AES")
//    cipher.init(Cipher.ENCRYPT_MODE, SpecKey)
//    val result = cipher.doFinal(plain).toList.map(String.format("%02x",_)).reduce(_+_).substring(0,32)
//    result
//  }

  private def castByteArray(input: Array[Byte], castSize: Int): Array[Byte] = {
    if(input.length == castSize){
      input
    }else if(input.length > castSize){
      input.takeRight(castSize)
    }else{
      Array.fill[Byte](castSize - input.length)(0x00) ++ input
    }
  }

  def block(keyLenght:Int, verbose: Boolean)(key: BigInt, block: BigInt, enc: Boolean) = if(enc) encryptBlock(key, block, keyLenght, verbose) else decryptBlock(key, block, keyLenght, verbose)

  def decryptBlock(key: BigInt, block: BigInt, keyLenght:Int, verbose: Boolean = false): BigInt = {

    // cast input key
    val keyModified = castByteArray(key.toByteArray, keyLenght / 8)

    val myKey = new SecretKeySpec(keyModified, "AES")

    // Create the cipher
    val desCipher = Cipher.getInstance("AES/ECB/NoPadding")

    // Initialize the cipher for encryption
    desCipher.init(Cipher.DECRYPT_MODE, myKey)

    // cast input block
    val blockCipher = castByteArray(block.toByteArray, 16)

    // Encrypt the text

    val blockPlain = desCipher.doFinal(blockCipher)

    if(verbose){
      println(s"Cipher : 0x${blockCipher.map(b => "%02X".format(b)).mkString("")}")
      println(s"KEY    : 0x${myKey.getEncoded().map(b => "%02X".format(b)).mkString("")}")
      println(s"Plain  : 0x${blockPlain.map(b  => "%02X".format(b)).mkString("")}")
      println("")
    }

    return BigInt(blockPlain.take(16))
  }

  def encryptBlock(key: BigInt, block: BigInt, keyLenght:Int, verbose: Boolean = false): BigInt = {

    // Cast the input key
    val keyModified = castByteArray(key.toByteArray, keyLenght / 8)
    val myKey       = new SecretKeySpec(keyModified, "AES")

    // Create the cipher
    val desCipher = Cipher.getInstance("AES/ECB/NoPadding")

    // Initialize the cipher for encryption
    desCipher.init(Cipher.ENCRYPT_MODE, myKey)

    // cast input block
    val blockPlain = castByteArray(block.toByteArray, 16)

    // Encrypt the text
    val blockCipher = desCipher.doFinal(blockPlain)

    if(verbose){
      println(s"Plain  : 0x${blockPlain.map(b  => "%02X".format(b)).mkString("")}")
      println(s"KEY    : 0x${myKey.getEncoded().map(b => "%02X".format(b)).mkString("")}")
      println(s"Cipher : 0x${blockCipher.map(b => "%02X".format(b)).mkString("")}")
      println("")
    }

    return BigInt(blockCipher.take(16))
  }
}

object BigIntToHexString {
  def apply(value: BigInt): String = s"0x${value.toByteArray.map(b => f"${b}%02X").mkString("")}"
}


class AESCoreTester extends AnyFunSuite {
  test("AESCoreStd_128"){
    val flags = VCSFlags(
      compileFlags = List("-kdb"),
      elaborateFlags = List("-kdb")
    )

    def initializeIO(dut: CryptoBlockIO): Unit ={
      dut.cmd.valid #= false
      dut.cmd.block.randomize()
      dut.cmd.key.randomize()
      if(dut.config.useEncDec) dut.cmd.enc.randomize()
    }


    def doSim(dut: CryptoBlockIO,clockDomain: ClockDomain, enc: Boolean, blockIn: BigInt = null, keyIn: BigInt = null)(refCrypto: (BigInt, BigInt, Boolean) => BigInt ): Unit = {
      val block_in = if(blockIn == null) BigInt(dut.cmd.block.getWidth, Random) else blockIn
      val key      = if(keyIn == null)   BigInt(dut.cmd.key.getWidth, Random)   else keyIn

      dut.cmd.valid #= true
      dut.cmd.block #= block_in
      dut.cmd.key #= key
      if(dut.config.useEncDec) dut.cmd.enc #= enc
      clockDomain.waitActiveEdge()
      dut.cmd.valid #= false
      dut.cmd.block #= BigInt(dut.cmd.block.getWidth, Random)
      dut.cmd.key #= BigInt(dut.cmd.block.getWidth, Random)
      waitUntil(dut.rsp.valid.toBoolean == true)
      val rtlBlock_out = dut.rsp.block.toBigInt
      val refBlock_out = refCrypto(key, block_in, enc)

      assert(BigInt(rtlBlock_out.toByteArray.takeRight(dut.cmd.block.getWidth / 8)) == BigInt(refBlock_out.toByteArray.takeRight(dut.cmd.block.getWidth / 8)) , s"Wrong result RTL ${BigIntToHexString(rtlBlock_out)} !=  REF ${BigIntToHexString(refBlock_out)}")

      // release the command valid between each transaction randomly
      clockDomain.waitActiveEdge()

      if(Random.nextBoolean()){
        initializeIO(dut)

        clockDomain.waitActiveEdge()
      }

    }

    SimConfig.withConfig(SpinalConfig(inlineRom = true)).withVCS(flags).withFSDBWave.compile(new AES_CORE(128 bits)).doSim { dut =>
      dut.clockDomain.forkStimulus(10000)
      initializeIO(dut.io)
      dut.clockDomain.waitActiveEdge()

      for(_ <- 0 to 20){
        doSim(dut.io, dut.clockDomain, enc = true )(AESREF.block(128, verbose = false))
      }

    }
  }
}
