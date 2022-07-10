package DataStream

import spinal.core.SpinalConfig

object MacMain extends App{
  SpinalConfig(targetDirectory = "rtl",oneFilePerComponent = true).generateVerilog(MacWordToByte())
}

