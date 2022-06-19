//***********************************//
//
//**********************************//

`timescale 1ns/1ps

module	round_transform5(
	input	[127:0]	round_datain,	 
	input 	[127:0] round_keyin,
	input			round_clk,
	input           round_rst_n,
	input           round_en,
	output			round_dataout_valid,
	output	[127:0]	round_dataout
);

wire	[31:0]	shift_out_3;
wire	[31:0]	shift_out_2;
wire	[31:0]	shift_out_1;
wire	[31:0]	shift_out_0;

//wire	[127:0]	subbytes_in;

wire	[31:0]	subbytes_out1;
wire	[31:0]	subbytes_out2;
wire	[31:0]	subbytes_out3;
wire	[31:0]	subbytes_out0;

wire	[31:0]	mixcolumns_out0;
wire	[31:0]	mixcolumns_out1;
wire	[31:0]	mixcolumns_out2;
wire	[31:0]	mixcolumns_out3;

wire 	[127:0]	mixcolumns_out;



	ShiftRows32 shiftrows(
		.shift_in	(round_datain	),
		
		.shift_clk		(round_clk	    ),
		.shift_rst_n	(round_rst_n	),
		.shift_en		(round_en	    ),			
		
		.shift_out3	(shift_out_3	),
		.shift_out2	(shift_out_2	),
		.shift_out1	(shift_out_1	),
		.shift_out0	(shift_out_0	)
	);

													
	SubBytes32	subBytes3(
		.clk			(round_clk		),
		.rst_n			(round_rst_n	),
		.subBytes_en	(round_en    	),	
		.subBytes_in	(shift_out_3	),
		.subBytes_out	(subbytes_out3	)
	);
	
	SubBytes32	subBytes2(
		.clk			(round_clk		),
		.rst_n			(round_rst_n	),
		.subBytes_en	(round_en    	),	
		.subBytes_in	(shift_out_2	),
		.subBytes_out	(subbytes_out2	)
	);
	
	SubBytes32	subBytes1(
		.clk			(round_clk		),
		.rst_n			(round_rst_n	),
		.subBytes_en	(round_en    	),	
		.subBytes_in	(shift_out_1	),
		.subBytes_out	(subbytes_out1	)
	);
	
	SubBytes32	subBytes0(
		.clk			(round_clk		),
		.rst_n			(round_rst_n	),
		.subBytes_en	(round_en    	),	
		.subBytes_in	(shift_out_0	),
		.subBytes_out	(subbytes_out0	)
	);
/****************************************************************/	
reg		[31:0]	R_subbytes_out0; 
reg		[31:0]	R_subbytes_out1; 
reg		[31:0]	R_subbytes_out2; 
reg		[31:0]	R_subbytes_out3; 
always@(posedge round_clk)
	begin
		if(~round_rst_n)
			begin
				R_subbytes_out0	<=	32'b0;
				R_subbytes_out1	<=	32'b0;
				R_subbytes_out2	<=	32'b0;
				R_subbytes_out3	<=	32'b0;
			end
		else if(round_en)
			begin
				R_subbytes_out0	<=	subbytes_out0;
				R_subbytes_out1	<=	subbytes_out1;
				R_subbytes_out2	<=	subbytes_out2;
				R_subbytes_out3	<=	subbytes_out3;
			end
		else
			begin
				R_subbytes_out0	<=	R_subbytes_out0;
				R_subbytes_out1	<=	R_subbytes_out1;
				R_subbytes_out2	<=	R_subbytes_out2;
				R_subbytes_out3	<=	R_subbytes_out3;
			end
	end
/****************************************************************/

/*********************************************************************************************/	
	MixColumn32	mixcolumn3(
		.mixcolumn32_in	(R_subbytes_out3	),
		.mixcolumn32_out(mixcolumns_out3	)
	);

	MixColumn32	mixcolumn2(
		.mixcolumn32_in	(R_subbytes_out2	),
		.mixcolumn32_out(mixcolumns_out2	)
	);
	
	MixColumn32	mixcolumn1(
		.mixcolumn32_in	(R_subbytes_out1	),
		.mixcolumn32_out(mixcolumns_out1	)
	);
	
	MixColumn32	mixcolumn0(
		.mixcolumn32_in	(R_subbytes_out0	),
		.mixcolumn32_out(mixcolumns_out0	)
	);
assign 	mixcolumns_out = {mixcolumns_out3,mixcolumns_out2,mixcolumns_out1,mixcolumns_out0};
	
reg		[127:0]	R_mixcolumns_out; 
always@(posedge round_clk)
	begin
		if(~round_rst_n)
			R_mixcolumns_out	<=	128'b0;
		else if(round_en)
			R_mixcolumns_out	<=	mixcolumns_out;
		else
			R_mixcolumns_out	<=	R_mixcolumns_out;
	end
	
/*********************************************************************************************/
reg [127:0]	round_dataout_mid;
reg round_dataout_valid_mid;
always@(posedge round_clk)
	begin
		if(~round_rst_n)
			begin
				round_dataout_mid			<=	128'b0;
				round_dataout_valid_mid		<=	1'b0;
			end
		else if(round_en && R_mixcolumns_out)
			begin
				round_dataout_mid			<=	R_mixcolumns_out ^ round_keyin;
				round_dataout_valid_mid		<=	1'b1;
			end
		else
			begin
				round_dataout_mid			<=	round_dataout_mid;
				round_dataout_valid_mid		<=	1'b0;
			end
	end

assign	round_dataout 			= round_dataout_mid;
assign  round_dataout_valid		= round_dataout_valid_mid;
	
endmodule
