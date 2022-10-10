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


case class CmdChannel(c:DmaCfg,withSlaveId:Boolean = false) extends Bundle with IMasterSlave {
  val id = if(withSlaveId) Bits(log2Up(c.slaveNode) bits) else null
  val startAddr = UInt(c.addrWidth bits)
  val wrOp = Bool()
  val transLen = UInt(c.lenWidth bits)
  val pri = UInt(c.priWidth bits)
  val reqVld = Bool()
  val reqReady = Bool()
  val transCancel = Bool()

  val rspStream = Stream(UInt(c.rspWidth bits))

  override def asMaster(): Unit = {
    if (withSlaveId) {
      in(id)
    }
    in(startAddr, wrOp, transLen, pri, reqVld, transCancel)
    out(reqReady)
    master(rspStream)
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