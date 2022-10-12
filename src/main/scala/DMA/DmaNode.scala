package DMA
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

object NodeType extends Enumeration{
  val fullVersion = 0
  val writeOnly = 1
  val readOnly = 2
}

case class DmaNodeInf(c:DmaCfg, nodeType:Int, withSlaveId:Boolean = false) extends Bundle with IMasterSlave {
  val cmd = CmdChannel(c,withSlaveId)
  val rsp = RspChannel(c,withSlaveId)
  val wrChannel = if(nodeType != NodeType.readOnly) WrChannel(c,withSlaveId) else null
  val rdChannel = if(nodeType != NodeType.writeOnly) RdChannel(c,withSlaveId) else null

  override def asMaster(): Unit = {
    master(cmd,wrChannel,rdChannel)
    slave(rsp)
  }

  def connect(a:DmaNodeInf,slaveId:UInt = 0) = {
    if (withSlaveId == a.withSlaveId) {
      cmd <> a.cmd
      wrChannel <> a.wrChannel
      rdChannel <> a.rdChannel
      rsp <> a.rsp
    } else if (withSlaveId == true) {
      for ((name, element)  <- a.cmd.cmdStream.elements) {
        val other = cmd.cmdStream.find(name)
        if (other != null) {
          element <> other
        }
      }

      for ((name, element)  <- a.rsp.rspStream.elements) {
        val other = rsp.rspStream.find(name)
        if (other != null) {
          element <> other
        }
      }

      for ((name, element)  <- a.wrChannel.wrStream.elements) {
        val other = wrChannel.wrStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }

      for ((name, element)  <- a.rdChannel.rdStream.elements) {
        val other = rdChannel.rdStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }

      if (cmd.cmdStream.id != null) {
        cmd.cmdStream.id := slaveId.asBits.resize(cmd.cmdStream.id.getWidth)
      }

      wrChannel.wrStream.fragment := slaveId.asBits.resize(cmd.cmdStream.id.getWidth) ## a.wrChannel.wrStream.fragment
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last
      a.rdChannel.rdStream.fragment := rdChannel.rdStream.fragment(c.dataWidth-1 downto 0)
      a.rdChannel.rdStream.last <> rdChannel.rdStream.last
    } else {
      for ((name, element)  <- a.cmd.elements) {
        val other = cmd.find(name)
        if (other != null) {
          element <> other
        }
      }

      for ((name, element)  <- a.rsp.rspStream.elements) {
        val other = rsp.rspStream.find(name)
        if (other != null) {
          element <> other
        }
      }

      for ((name, element)  <- a.wrChannel.wrStream.elements) {
        val other = wrChannel.wrStream.find(name)
        if (other != null && !name.contains("payload")) {
          element <> other
        }
      }

      for ((name, element)  <- a.rdChannel.rdStream.elements) {
        val other = rdChannel.rdStream.find(name)
        if (other != null  && !name.contains("payload")) {
          element <> other
        }
      }
      if (a.cmd.cmdStream.id != null) {
        a.cmd.cmdStream.id := slaveId.asBits.resize(a.cmd.cmdStream.id.getWidth)
      }

      if(rsp.rspStream.id != null) {
        rsp.rspStream.id := slaveId.asBits.resize(a.cmd.cmdStream.id.getWidth)
      }

      wrChannel.wrStream.fragment <> a.wrChannel.wrStream.fragment(c.dataWidth-1 downto 0)
      wrChannel.wrStream.last <> a.wrChannel.wrStream.last
      a.rdChannel.rdStream.fragment <> slaveId.asBits.resize(a.cmd.cmdStream.id.getWidth) ## rdChannel.rdStream.fragment
      a.rdChannel.rdStream.last <> rdChannel.rdStream.last
    }
  }
}


case class NodeExceptionDetecter(c:DmaCfg) extends Component{

  val io = new Bundle{
    val node = master(DmaNodeInf(c,NodeType.fullVersion))
    val nodeOut = slave(DmaNodeInf(c,NodeType.fullVersion))
  }


  val cmdFire = io.node.cmd.cmdStream.valid && io.node.cmd.cmdStream.ready
  //exception detect
  val dirLatch = RegNextWhen(io.node.cmd.cmdStream.wrOp,cmdFire,init = False)
  val transLenLatch = RegNextWhen(io.node.cmd.cmdStream.transLen,cmdFire)
  val transCnt = Reg(UInt(c.lenWidth bits)) init 0
  val goldenLastFlag = False
  val forceLast = False
  val forceVld = False
  val forceReady = False
  val forceRsp = False
  val forceCode = Bits(c.rspWidth bits)
  val lastUnmatchErr = False
  val dir = cmdFire ? io.node.cmd.cmdStream.wrOp | dirLatch
  val watchDog = UInt(10 bits)

  when((dir && io.nodeOut.wrChannel.wrStream.last) || (!dir && io.node.rdChannel.rdStream.last) || io.node.rsp.rspStream.valid) {
    transCnt := 0
  } elsewhen(cmdFire) {
    when(io.node.cmd.cmdStream.wrOp) {
      when(io.node.wrChannel.wrStream.fire) {
        transCnt := 1
      } otherwise {
        transCnt := 0
      }
    } otherwise {
      when(io.node.rdChannel.rdStream.fire) {
        transCnt := 1
      } otherwise {
        transCnt := 0
      }
    }
  } otherwise {
    when(io.node.cmd.cmdStream.wrOp) {
      when(io.node.wrChannel.wrStream.fire) {
        transCnt := transCnt + 1
      }
    } otherwise {
      when(io.node.rdChannel.rdStream.fire) {
        transCnt := transCnt + 1
      }
    }
  }

  //gen golden last flag accord translen
  when(cmdFire) {
    when(io.node.cmd.cmdStream.transLen === 1){
      goldenLastFlag := True
    } otherwise {
      goldenLastFlag := False
    }
  } otherwise {
    goldenLastFlag := transCnt === (transLenLatch - 1)
  }

  when(dir) {
    // last flag dont match
    when((goldenLastFlag =/= io.node.wrChannel.wrStream.last) && io.node.wrChannel.wrStream.valid) {
      lastUnmatchErr := True
      assert(False, "wr channel last flag miss match\n" + "goldenLastFlag is" + goldenLastFlag)
      when(goldenLastFlag) {
        // node stream lost last flag
        forceLast := True
      }
    }
  }

  for ((name,element) <- io.node.elements) {
    val target =io.nodeOut.find(name)
    if (!name.contains("rspStream")) {
      target <> element
    }
  }

  io.nodeOut.rsp.rspStream.ready <> io.node.rsp.rspStream.ready
  io.node.rsp.rspStream.valid := forceRsp | io.nodeOut.rsp.rspStream.valid
  io.node.rsp.rspStream.payload := forceRsp ? forceCode.asUInt | io.nodeOut.rsp.rspStream.payload

  io.nodeOut.wrChannel.wrStream.last := forceLast | io.node.wrChannel.wrStream.last
  io.nodeOut.wrChannel.wrStream.payload <> io.node.wrChannel.wrStream.payload
  io.nodeOut.wrChannel.wrStream.ready <> io.node.wrChannel.wrStream.ready
  io.nodeOut.wrChannel.wrStream.valid := forceVld | io.node.wrChannel.wrStream.valid

  io.nodeOut.rdChannel.rdStream.last <> io.node.rdChannel.rdStream.last
  io.nodeOut.rdChannel.rdStream.payload <> io.node.rdChannel.rdStream.payload
  io.node.rdChannel.rdStream.ready := forceReady | io.nodeOut.rdChannel.rdStream.ready
  io.nodeOut.rdChannel.rdStream.valid <>  io.node.rdChannel.rdStream.valid


  // detect long time no wr valid
  when(dir) {
    when(transCnt =/= 0) {
      when(io.node.wrChannel.wrStream.valid) {
        watchDog := 0
      } otherwise {
        watchDog := watchDog + 1
      }
    } otherwise {
      watchDog := 0
    }
  } otherwise{
    when(transCnt =/= 0) {
      when(io.node.rdChannel.rdStream.ready) {
        watchDog := 0
      } otherwise {
        watchDog := watchDog + 1
      }
    } otherwise {
      watchDog := 0
    }
  }

  val watchDogToFlag = RegInit(False)
  when(transCnt === 0) {
    watchDogToFlag := False
  } elsewhen(watchDog === 0x3ff) {
    watchDogToFlag := True
  }

  when(watchDogToFlag) {
    assert(False,"watch dog time out")
    when(dir) {
      forceVld := True
    } otherwise {
      forceReady := True
    }
  }

  when(watchDogToFlag.rise()) {
    forceRsp := True
    forceCode := 1<<(c.rspWidth-1)
  } elsewhen(lastUnmatchErr.rise()) {
    forceRsp := True
    forceCode := 1<<(c.rspWidth-2)
  }
}

