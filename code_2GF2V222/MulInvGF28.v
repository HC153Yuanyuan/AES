`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    21:26:22 10/03/2014 
// Design Name: 
// Module Name:    SquMulLambda 
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
module MulInvGF28(
    input [7:0] A_GF28,
    output wire [7:0] D_GF28
    );
	 
	wire [3:0] AH;  
	assign AH = A_GF28[7:4];
	wire [3:0] AL;  
	assign AL = A_GF28[3:0];
	
	wire [3:0] C_GF24;
	assign C_GF24 = AH ^ AL;
	
	wire [3:0] E_GF24;	
	SquMulPhi U1(
		.A_GF24		(C_GF24),
		.B_GF24		(E_GF24));
		
	wire [3:0] F_GF24;
	MulGF24 U2(
		.A_GF24		(AH),
		.B_GF24		(AL),
		.D_GF24		(F_GF24));
		
	wire [3:0] H_GF24;
	assign H_GF24 = E_GF24 ^ F_GF24;	
		
	wire [3:0] I_GF24;
	MulInvGF24 U3(
		.A_GF24		(H_GF24),
		.B_GF24		(I_GF24));
		
	wire [3:0] DL;
	MulGF24 U4(
		.A_GF24		(AH),
		.B_GF24		(I_GF24),
		.D_GF24		(DL));
		
	wire [3:0] DH;
	MulGF24 U5(
		.A_GF24		(AL),
		.B_GF24		(I_GF24),
		.D_GF24		(DH));
		
   assign D_GF28[7:4] = DH;
   assign D_GF28[3:0] = DL;
	

endmodule
