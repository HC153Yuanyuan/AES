`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    20:26:22 10/03/2014 
// Design Name: 
// Module Name:    IsoMaMat 
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
module IsoMapMat(
    input [7:0] Din,
    output wire [7:0] Dout
    );
	 
	 assign Dout[7] = Din[7] ^ Din[6] ^ Din[5] ^ Din[2] ^ Din[1] ^ Din[0];
	 assign Dout[6] = Din[6] ^ Din[5] ^ Din[4] ^ Din[0];
	 assign Dout[5] = Din[6] ^ Din[5] ^ Din[1] ^ Din[0];
	 assign Dout[4] = Din[7] ^ Din[6] ^ Din[5] ^ Din[0];
	 assign Dout[3] = Din[7] ^ Din[4] ^ Din[3] ^ Din[1] ^ Din[0];
	 assign Dout[2] = Din[0];
	 assign Dout[1] = Din[6] ^ Din[5] ^ Din[0]; 
	 assign Dout[0] = Din[6] ^ Din[3] ^ Din[2] ^ Din[1] ^ Din[0];
	 


endmodule
