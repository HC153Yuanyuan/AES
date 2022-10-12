package DMA

import spinal.core._
import spinal.lib._
import spinal.lib.experimental.chisel.Bundle

object DmaImplicit {
  implicit def transform(d : Int ):BigInt = {
    BigInt(d)
  }

  implicit def transformFloat(d : Float ):Int = {
    d.toInt
  }

  implicit def transformDouble(d : Double ):Int = {
    d.toInt
  }
}


case class DmaCfg(
  val addrWidth:Int = 20,
  val dataWidth:Int = 32,
  val priWidth:Int = 2,
  val lenWidth:Int = log2Up(32),
  val rspWidth:Int = 2,
  val slaveNode:Int = 2
)

case class RspInfo(c:DmaCfg,withSlaveId:Boolean = false) extends Bundle {
  val id = if(withSlaveId) Bits(log2Up(c.slaveNode) bits) else null
  val rspCode = UInt(c.rspWidth bits)
}

case class RspChannel(c:DmaCfg,withSlaveId:Boolean = false) extends Bundle with IMasterSlave {
  val rspStream = Stream(RspInfo(c,withSlaveId))
  override def asMaster(): Unit = {
    rspStream.asMaster()
  }
}

case class CmdInfo(c:DmaCfg,withSlaveId:Boolean = false) extends Bundle {
  val id = if(withSlaveId) Bits(log2Up(c.slaveNode) bits) else null
  val startAddr = UInt(c.addrWidth bits)
  val wrOp = Bool()
  val transLen = UInt(c.lenWidth bits)
  val pri = UInt(c.priWidth bits)
}



case class CmdChannel(c:DmaCfg,withSlaveId:Boolean = false) extends Bundle with IMasterSlave {
  val cmdStream = Stream(CmdInfo(c,withSlaveId))

  override def asMaster(): Unit = {
    cmdStream.asMaster()
  }
}


case class WrChannel(c:DmaCfg,withSlaveId:Boolean = false) extends  Bundle with IMasterSlave {
  // wrdata stream
  val wrStream = if (!withSlaveId) Stream(Fragment(Bits(c.dataWidth bits))) else Stream(Fragment(Bits(c.dataWidth + log2Up(c.slaveNode) bits)))

  override def asMaster(): Unit = {
    wrStream.asSlave()
  }
}

case class RdChannel(c:DmaCfg,withSlaveId:Boolean = false) extends  Bundle with IMasterSlave {
  val rdStream = if (!withSlaveId) Stream(Fragment(Bits(c.dataWidth bits))) else Stream(Fragment(Bits(c.dataWidth + log2Up(c.slaveNode) bits)))

  override def asMaster(): Unit = {
    rdStream.asMaster()
  }
}