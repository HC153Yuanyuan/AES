`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    10:15:30 10/04/2014 
// Design Name: 
// Module Name:    InvIsoMap_Aff_Mat 
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
module InvIsoMap_Aff_Mat(
    input [7:0] Din,
    output wire [7:0] Dout
    );
	 
	 assign Dout[7] = Din[5] ^ Din[3];
	 assign Dout[6] = Din[7] ^ Din[3];
	 assign Dout[5] = Din[6] ^ Din[0];
	 assign Dout[4] = Din[7] ^ Din[5] ^ Din[3];
	 assign Dout[3] = Din[7] ^ Din[6] ^ Din[5] ^ Din[4] ^ Din[3];
	 assign Dout[2] = Din[6] ^ Din[5] ^ Din[3] ^ Din[2] ^ Din[0];
	 assign Dout[1] = Din[5] ^ Din[4] ^ Din[1]; 
	 assign Dout[0] = Din[6] ^ Din[4] ^ Din[1];


endmodule
