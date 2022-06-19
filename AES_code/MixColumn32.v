
`timescale 1ns/1ps
//`include "./MixColumn8.v"

module	MixColumn32(
	input	[31:0]	mixcolumn32_in,
	//input			clk,
	//input			rst,
	//input			enable,
	//output			finish,
	output	[31:0]	mixcolumn32_out
);

wire	[7:0]	xout3,xout2,xout1,xout0;
  
wire	[7:0]	xin3	=	mixcolumn32_in[31:24];
wire	[7:0]	xin2	=	mixcolumn32_in[23:16];
wire	[7:0]	xin1	=	mixcolumn32_in[15:8];
wire	[7:0]	xin0	=	mixcolumn32_in[7:0];


MixColumn8 mix8_3(
	.in3(xin3),.in2(xin2),.in1(xin1),.in0(xin0),.outx(xout3)
);
MixColumn8 mix8_2(
	.in3(xin2),.in2(xin1),.in1(xin0),.in0(xin3),.outx(xout2)
);
MixColumn8 mix8_1(
	.in3(xin1),.in2(xin0),.in1(xin3),.in0(xin2),.outx(xout1)
);
MixColumn8 mix8_0(
	.in3(xin0),.in2(xin3),.in1(xin2),.in0(xin1),.outx(xout0)
);

assign	mixcolumn32_out	=	{xout3,xout2,xout1,xout0};


endmodule
