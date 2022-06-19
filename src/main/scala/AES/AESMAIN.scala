package AES
import spinal.core._

object AESMAIN extends App {
  SpinalConfig(targetDirectory = "rtl",oneFilePerComponent = true).generateVerilog(AES_CORE(128 bits))
}
