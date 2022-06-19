`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    08:50:23 10/04/2014 
// Design Name: 
// Module Name:    MulGF24 
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
module MulGF24(
    input [3:0] A_GF24,
    input [3:0] B_GF24,
    output wire [3:0] D_GF24
    );

	wire [1:0] AH, AL, BH, BL;
	assign AH = A_GF24[3:2];
	assign AL = A_GF24[1:0];
	assign BH = B_GF24[3:2];
	assign BL = B_GF24[1:0];
	
	wire [1:0] E_GF22, F_GF22;
	assign E_GF22 = AH ^ AL;
	assign F_GF22 = BH ^ BL;	
	
	wire [1:0] G_GF22;
	MulGF22 U1(
		.A_GF22		(E_GF22),
		.B_GF22		(F_GF22),
		.D_GF22		(G_GF22));
		
	wire [1:0] I_GF22;
	MulLambda U2(
		.A_GF22		(G_GF22),
		.B_GF22		(I_GF22));	
		
	wire [1:0] J_GF22;
	MulGF22 U3(
		.A_GF22		(AH),
		.B_GF22		(BH),
		.D_GF22		(J_GF22));
			
	wire [1:0] K_GF22;
	MulGF22 U4(
		.A_GF22		(AL),
		.B_GF22		(BL),
		.D_GF22		(K_GF22));
		
	wire [1:0] DH, DL;
	assign DH = J_GF22 ^ I_GF22;
	assign DL = K_GF22 ^ I_GF22;
	
	assign D_GF24[3:2] = DH;
	assign D_GF24[1:0] = DL;	

endmodule
