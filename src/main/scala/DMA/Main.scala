package DMA


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.AhbLite3Config


object Main extends App{
  val cfg = new MacDmaConfig(addressWidth = 32, dataWidth = 32, enableRx = true, enableTx = false,burstCountWidth = 4)
  val dmaCfg = DmaCfg(slaveNode = 8)
  val ahbCfg = AhbLite3Config(addressWidth = dmaCfg.addrWidth,dataWidth = dmaCfg.dataWidth)

  SpinalConfig(targetDirectory = "rtl",oneFilePerComponent = true).generateVerilog(DmaArbWrapper(dmaCfg))
}

