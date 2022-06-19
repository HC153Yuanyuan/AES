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
module SquMulLambda(
    input [1:0] A_GF22,
    output wire [1:0] B_GF22
    );

	assign  B_GF22[1] =   A_GF22[1];
	assign  B_GF22[0] =   A_GF22[0] ^ A_GF22[1];	

endmodule
