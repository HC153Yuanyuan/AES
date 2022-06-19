
  `timescale 1ns/1ps
module xtime(
	input	[7:0]	xtime_input,
	output	[7:0]	xtime_output
);
/*
wire	outvar7, outvar6, outvar5, outvar4, outvar3, outvar2, outvar1, outvar0;

assign	outvar7	=	xtime_input[6];
assign	outvar6	=	xtime_input[5];
assign	outvar5	=	xtime_input[4];
assign	outvar4	=	xtime_input[7] ^ xtime_input[3];
assign	outvar3	=	xtime_input[7] ^ xtime_input[2];
assign	outvar2	=	xtime_input[1];
assign	outvar1	=	xtime_input[7] ^ xtime_input[0];
assign	outvar0	=	xtime_input[7];	

assign	xtime_output	=	{outvar7, outvar6, outvar5, outvar4, outvar3, outvar2, outvar1, outvar0};
*/

assign	xtime_output[7]	=	xtime_input[6];
assign	xtime_output[6]	=	xtime_input[5];
assign	xtime_output[5]	=	xtime_input[4];
assign	xtime_output[4]	=	xtime_input[7] ^ xtime_input[3];
assign	xtime_output[3]	=	xtime_input[7] ^ xtime_input[2];
assign	xtime_output[2]	=	xtime_input[1];
assign	xtime_output[1]	=	xtime_input[7] ^ xtime_input[0];
assign	xtime_output[0]	=	xtime_input[7];	

endmodule
