package DMA


import spinal.core._
import spinal.lib._


object Main extends App{
  val cfg = new MacDmaConfig(addressWidth = 32, dataWidth = 32, enableRx = true, enableTx = false,burstCountWidth = 4)
  val dmaCfg = DmaCfg(slaveNode = 8)

  SpinalConfig(targetDirectory = "rtl").generateVerilog(DmaArbiter(dmaCfg))
}

