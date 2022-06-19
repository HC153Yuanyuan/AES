`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    21:18:22 10/03/2014 
// Design Name: 
// Module Name:    MultInvGF22 
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
module MulInvGF22(
    input [1:0] A_GF22,
    output wire [1:0] B_GF22
    );
	 
	
	assign  B_GF22[1] =   A_GF22[0];
	assign  B_GF22[0] =   A_GF22[1];	
	
endmodule