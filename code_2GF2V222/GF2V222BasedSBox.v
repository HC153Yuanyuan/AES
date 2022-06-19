`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    10:35:38 10/04/2014 
// Design Name: 
// Module Name:    GF2V222BasedSBox 
// Project Name: 
// Target Devices: 
// Tool versions: 
// Description: 
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////
module GF2V222BasedSBox(
    input [7:0] Sin,
    output wire [7:0] Sout
    );
	 
	 wire [7:0] IsoOut;
	 IsoMapMat U1(
		.Din		(Sin),
		.Dout		(IsoOut));
		
	 wire [7:0] MulInvOut;
	 MulInvGF28 U2(
		.A_GF28	(IsoOut),
		.D_GF28	(MulInvOut));
		
	 wire [7:0] InvIsoout;
	 InvIsoMap_Aff_Mat U3(
		.Din		(MulInvOut),
		.Dout		(InvIsoout));
		
	 assign Sout[7] = InvIsoout[7];
	 assign Sout[6] = ~(InvIsoout[6]);
	 assign Sout[5] = ~(InvIsoout[5]);
	 assign Sout[4] = InvIsoout[4];
	 assign Sout[3] = InvIsoout[3];
	 assign Sout[2] = InvIsoout[2];
	 assign Sout[1] = ~(InvIsoout[1]);
	 assign Sout[0] = ~(InvIsoout[0]);


endmodule
