package AES_256

import spinal.core.{Bits, _}

object Sbox {

  private def InvIsoMap_Aff_Mat(Din: Bits): Bits = {
    val Dout = Bits(8 bits)
     Dout(7) := Din(5) ^ Din(3)
     Dout(6) := Din(7) ^ Din(3)
     Dout(5) := Din(6) ^ Din(0)
     Dout(4) := Din(7) ^ Din(5) ^ Din(3)
     Dout(3) := Din(7) ^ Din(6) ^ Din(5) ^ Din(4) ^ Din(3)
     Dout(2) := Din(6) ^ Din(5) ^ Din(3) ^ Din(2) ^ Din(0)
     Dout(1) := Din(5) ^ Din(4) ^ Din(1)
     Dout(0) := Din(6) ^ Din(4) ^ Din(1)
     Dout
  }

  private def IsoMapMat(Din: Bits): Bits = {
    val Dout = Bits(8 bits)
     Dout(7) := Din(7) ^ Din(6) ^ Din(5) ^ Din(2) ^ Din(1) ^ Din(0)
     Dout(6) := Din(6) ^ Din(5) ^ Din(4) ^ Din(0)
     Dout(5) := Din(6) ^ Din(5) ^ Din(1) ^ Din(0)
     Dout(4) := Din(7) ^ Din(6) ^ Din(5) ^ Din(0)
     Dout(3) := Din(7) ^ Din(4) ^ Din(3) ^ Din(1) ^ Din(0)
     Dout(2) := Din(0)
     Dout(1) := Din(6) ^ Din(5) ^ Din(0)
     Dout(0) := Din(6) ^ Din(3) ^ Din(2) ^ Din(1) ^ Din(0)
     Dout
  }

  private def MulLambda(A_GF22:Bits):Bits = {
    val B_GF22 = Bits(2 bits)

    B_GF22(1) :=   A_GF22(0)
    B_GF22(0) :=   A_GF22(0) ^ A_GF22(1)

    B_GF22
  }

  private def MulGF22(A_GF22:Bits, B_GF22:Bits): Bits = {
    val C_GF22 = Bool
    val D_GF22 = Bits(2 bits)
    C_GF22 := (B_GF22(0) &  A_GF22(1)) ^ (A_GF22(0) &  B_GF22(1))
    D_GF22(1) := (B_GF22(0) &  A_GF22(0)) ^ C_GF22
    D_GF22(0) := (B_GF22(1) &  A_GF22(1)) ^ C_GF22
    D_GF22
  }

  private def MulInvGF22(A_GF22:Bits):Bits = {
    val B_GF22 = Bits(2 bits)

    B_GF22(1) :=   A_GF22(0)
    B_GF22(0) :=   A_GF22(1)

    B_GF22
  }

  private def SquMulLambda(A_GF22:Bits):Bits = {
    val B_GF22 = Bits(2 bits)

    B_GF22(1) :=   A_GF22(1)
    B_GF22(0) :=   A_GF22(0) ^ A_GF22(1)

    B_GF22
  }

  private def SquMulPhi(A_GF24:Bits):Bits = {
    val B_GF24 = Bits(4 bits)
    B_GF24(3) :=   A_GF24(2) ^ A_GF24(0)
    B_GF24(2) :=   A_GF24(3) ^ A_GF24(1)
    B_GF24(1) :=   A_GF24(1) ^ A_GF24(0)
    B_GF24(0) :=   A_GF24(0)
    B_GF24
  }

  private def MulGF24(A_GF24:Bits, B_GF24:Bits):Bits = {
    val D_GF24 = Bits(4 bits)
    val AH, AL, BH, BL= Bits(2 bits)

    AH := A_GF24(3 downto 2)
    AL := A_GF24(1 downto 0)
    BH := B_GF24(3 downto 2)
    BL := B_GF24(1 downto 0)

    val E_GF22, F_GF22 = Bits(2 bits)
    E_GF22 := AH ^ AL
    F_GF22 := BH ^ BL

    val G_GF22 = Bits(2 bits)

    G_GF22 := MulGF22(A_GF22 = E_GF22,B_GF22 = F_GF22)

    val I_GF22 = Bits(2 bits)

    I_GF22 := MulLambda(G_GF22)

    val J_GF22 = Bits(2 bits)
    J_GF22 :=MulGF22(A_GF22 = AH,B_GF22 = BH)

    val K_GF22 = Bits(2 bits)
    K_GF22 :=MulGF22(A_GF22 = AL,B_GF22 = BL)

    val DH, DL= Bits(2 bits)
	  DH := J_GF22 ^ I_GF22
	  DL := K_GF22 ^ I_GF22

	  D_GF24(3 downto 2) := DH
	  D_GF24(1 downto 0) := DL

    D_GF24

  }

  private def MulInvGF24(A_GF24:Bits): Bits = {
    val B_GF24 = Bits(4 bits)
    val  AH = Bits(2 bits)
	  AH := A_GF24(3 downto 2)
	  val AL = Bits(2 bits)
	  AL := A_GF24(1 downto 0)

	  val C_GF22= Bits(2 bits)
	  C_GF22 := AH ^ AL

	  val E_GF22= Bits(2 bits)
	  E_GF22 := SquMulLambda(A_GF22 = C_GF22)

	  val F_GF22= Bits(2 bits)
    F_GF22 := MulGF22(A_GF22=AH,B_GF22=AL)


	  val H_GF22= Bits(2 bits)
	  H_GF22 := E_GF22 ^ F_GF22;

	  val I_GF22= Bits(2 bits)
    I_GF22 := MulInvGF22(A_GF22=H_GF22)


	  val BL= Bits(2 bits)
    BL := MulGF22(A_GF22 = AH,B_GF22 = I_GF22)


    val BH= Bits(2 bits)
    BH := MulGF22(A_GF22 = AL, B_GF22 =  I_GF22)

    B_GF24(3 downto 2) := BH
    B_GF24(1 downto 0) := BL

    B_GF24
  }

  private def MulInvGF28(A_GF28:Bits):Bits = {
    val D_GF28 = Bits(8 bits)
    	val  AH = Bits(4 bits)
	    AH := A_GF28(7 downto 4)
	    val  AL = Bits(4 bits)
	    AL := A_GF28(3 downto 0)

	    val C_GF24  = Bits(4 bits)
	    C_GF24 := AH ^ AL

	    val E_GF24 = Bits(4 bits)
      E_GF24 := SquMulPhi(C_GF24)

	    val F_GF24  = Bits(4 bits)
      F_GF24 := MulGF24(A_GF24 = AH,	B_GF24	= AL)


	    val H_GF24 = Bits(4 bits)
	    H_GF24 := E_GF24 ^ F_GF24;

	    val I_GF24 = Bits(4 bits)
      I_GF24 := MulInvGF24(H_GF24)

	    val DL = Bits(4 bits)
      DL := MulGF24(A_GF24 = AH, B_GF24 = I_GF24)


	    val DH = Bits(4 bits)
      DH := MulGF24(A_GF24 = AL, B_GF24 = I_GF24)


      D_GF28(7 downto 4) := DH
      D_GF28(3 downto 0) := DL
      D_GF28
  }

  def GF2V222BasedSBox(Sin:Bits):Bits = {
    val Sout = Bits(8 bits)
    val IsoOut = Bits(8 bits)
    IsoOut := IsoMapMat(Sin)
    val MulInvOut = Bits(8 bits)
    MulInvOut := MulInvGF28(IsoOut)

    val InvIsoout = Bits(8 bits)
    InvIsoout := InvIsoMap_Aff_Mat(MulInvOut)

    Sout(7) := InvIsoout(7);
    Sout(6) := ~(InvIsoout(6));
    Sout(5) := ~(InvIsoout(5));
    Sout(4) := InvIsoout(4);
    Sout(3) := InvIsoout(3);
    Sout(2) := InvIsoout(2);
    Sout(1) := ~(InvIsoout(1));
    Sout(0) := ~(InvIsoout(0));

    Sout

  }

  def Sbox32(Sin:Bits):Bits = {
    val Sout = Bits(32 bits)
    for (i <- (0 to 3)) {
      Sout(8*i,8 bits) := GF2V222BasedSBox(Sin(8*i,8 bits))
    }
    Sout
  }
}


case class Sbox32() extends Component {
  val io = new Bundle  {
    val dataIn = in Bits(32 bits)
    val dataOut = out Bits(32 bits)
  }
  io.dataOut := Sbox.Sbox32(io.dataIn)

  def driveFrom(dataIn:Bits):Bits = {
    io.dataIn := dataIn
    io.dataOut
  }
}
