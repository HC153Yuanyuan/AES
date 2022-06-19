
`timescale 1ns/1ps

module	MixColumn8(
	input	[7:0]	in3,
	input	[7:0]	in2,
	input	[7:0]	in1,
	input	[7:0]	in0,
	output	[7:0]	outx	
);

wire		[7:0]	w1,w2,w3,outx_var;

/*
//define function xtime
function 	[7:0] 	xtime;
	input	[7:0]	data_in;
	reg 	[3:0] 	xtime_t;
	
	begin
		xtime[7:5] 	= data_in[6:4];
		xtime_t[3] 	= data_in[7];
		xtime_t[2] 	= data_in[7];
		xtime_t[1] 	= 0;
		xtime_t[0] 	= data_in[7];
		xtime[4:1] 	= xtime_t^data_in[3:0];
		xtime[0] 	= data_in[7];
	end	
endfunction 
*/

  xtime	xtime_fun(
	.xtime_input	(w1),
	.xtime_output	(w3)
  );
  
  
assign	  w1		=	in3^in2;
assign	  w2		=	in1^in0;
//assign	  w3		=	xtime(w1);
assign	 outx_var	=	in2^w2^w3;

assign	outx	=	outx_var;

endmodule
