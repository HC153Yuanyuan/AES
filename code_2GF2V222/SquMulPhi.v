`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    09:37:44 10/04/2014 
// Design Name: 
// Module Name:    SquMulPhi 
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
module SquMulPhi(
    input [3:0] A_GF24,
    output wire [3:0] B_GF24
    );

	assign  B_GF24[3] =   A_GF24[2] ^ A_GF24[0];
	assign  B_GF24[2] =   A_GF24[3] ^ A_GF24[1];
	assign  B_GF24[1] =   A_GF24[1] ^ A_GF24[0];	
	assign  B_GF24[0] =   A_GF24[0];		



endmodule
