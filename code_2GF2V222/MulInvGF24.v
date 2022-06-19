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
module MulInvGF24(
    input [3:0] A_GF24,
    output wire [3:0] B_GF24
    );
	 
	wire [1:0] AH;  
	assign AH = A_GF24[3:2];
	wire [1:0] AL;  
	assign AL = A_GF24[1:0];
	
	wire [1:0] C_GF22;
	assign C_GF22 = AH ^ AL;
	
	wire [1:0] E_GF22;	
	SquMulLambda U1(
		.A_GF22		(C_GF22),
		.B_GF22		(E_GF22));
		
	wire [1:0] F_GF22;
	MulGF22 U2(
		.A_GF22		(AH),
		.B_GF22		(AL),
		.D_GF22		(F_GF22));
		
	wire [1:0] H_GF22;
	assign H_GF22 = E_GF22 ^ F_GF22;	
		
	wire [1:0] I_GF22;
	MulInvGF22 U3(
		.A_GF22		(H_GF22),
		.B_GF22		(I_GF22));
		
	wire [1:0] BL;
	MulGF22 U4(
		.A_GF22		(AH),
		.B_GF22		(I_GF22),
		.D_GF22		(BL));
		
	wire [1:0] BH;
	MulGF22 U5(
		.A_GF22		(AL),
		.B_GF22		(I_GF22),
		.D_GF22		(BH));
		
   assign B_GF24[3:2] = BH;
   assign B_GF24[1:0] = BL;
	

endmodule
