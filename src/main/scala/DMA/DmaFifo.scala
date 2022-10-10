package DMA

import spinal.core.{ClockDomain, Component}

case class DmaFifo(c:DmaCfg,Depth:Int,nodeDomain:ClockDomain,busDomain:ClockDomain) extends Component {
