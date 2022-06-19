`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    09:02:34 10/04/2014 
// Design Name: 
// Module Name:    MulLambda 
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
module MulLambda(
    input [1:0] A_GF22,
    output [1:0] B_GF22
    );
	 
	assign  B_GF22[1] =   A_GF22[0];
	assign  B_GF22[0] =   A_GF22[0] ^ A_GF22[1];	

endmodule
