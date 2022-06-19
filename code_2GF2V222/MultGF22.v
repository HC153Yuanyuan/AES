`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    20:56:41 10/03/2014 
// Design Name: 
// Module Name:    Mult_GF2_2 
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
module MulGF22(
    input [1:0] A_GF22,
    input [1:0] B_GF22,
    output wire [1:0] D_GF22
    );
	 
	wire C_GF22;
	assign  C_GF22 = (B_GF22[0] &  A_GF22[1]) ^ (A_GF22[0] &  B_GF22[1]);
	assign  D_GF22[1] = (B_GF22[0] &  A_GF22[0]) ^ C_GF22;
	assign  D_GF22[0] = (B_GF22[1] &  A_GF22[1]) ^ C_GF22;	
	
	
endmodule
