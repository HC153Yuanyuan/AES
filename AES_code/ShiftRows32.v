`timescale 1ns/1ps
////////////////////////////////////////////////////////////////////////
module ShiftRows32(
	input	[127:0]	shift_in,
	input			shift_clk,
	input			shift_rst_n,
	input			shift_en,
	output	[31:0]	shift_out3,
	output	[31:0]	shift_out2,
	output	[31:0]	shift_out1,
	output	[31:0]	shift_out0
);

wire	[7:0]	data_var0	=	shift_in[7:0]; 
wire	[7:0]	data_var1	=	shift_in[15:8];
wire	[7:0]	data_var2	=	shift_in[23:16];
wire	[7:0]	data_var3	=	shift_in[31:24];
wire	[7:0]	data_var4	=	shift_in[39:32];
wire	[7:0]	data_var5	=	shift_in[47:40];
wire	[7:0]	data_var6	=	shift_in[55:48];
wire	[7:0]	data_var7	=	shift_in[63:56];
wire	[7:0]	data_var8	=	shift_in[71:64];
wire	[7:0]	data_var9	=	shift_in[79:72];
wire	[7:0]	data_var10	=	shift_in[87:80];
wire	[7:0]	data_var11	=	shift_in[95:88];
wire	[7:0]	data_var12	=	shift_in[103:96];
wire	[7:0]	data_var13	=	shift_in[111:104];
wire	[7:0]	data_var14	=	shift_in[119:112];
wire	[7:0]	data_var15	=	shift_in[127:120];

wire	[7:0]	data_out_var15	=	data_var15	;
wire	[7:0]	data_out_var14	=	data_var10	;
wire	[7:0]	data_out_var13	=	data_var5	;
wire	[7:0]	data_out_var12	=	data_var0	;
wire	[7:0]	data_out_var11	=	data_var11	;
wire	[7:0]	data_out_var10	=	data_var6	;
wire	[7:0]	data_out_var9	=	data_var1	;
wire	[7:0]	data_out_var8	=	data_var12	;
wire	[7:0]	data_out_var7	=	data_var7	;
wire	[7:0]	data_out_var6	=	data_var2	;
wire	[7:0]	data_out_var5	=	data_var13	;
wire	[7:0]	data_out_var4	=	data_var8	;
wire	[7:0]	data_out_var3	=	data_var3	;
wire	[7:0]	data_out_var2	=	data_var14	;
wire	[7:0]	data_out_var1	=	data_var9	;
wire	[7:0]	data_out_var0	=	data_var4	;

reg [31:0] shift_out3_mid;
reg [31:0] shift_out2_mid;
reg [31:0] shift_out1_mid;
reg [31:0] shift_out0_mid;

    always@(posedge shift_clk)
        begin
            if(!shift_rst_n)
               begin
                  shift_out3_mid     <= 32'b0;
                  shift_out2_mid     <= 32'b0;
                  shift_out1_mid     <= 32'b0;     
				  shift_out0_mid     <= 32'b0;
               end   
            else if(shift_en)
               begin
                  shift_out3_mid     <= {data_out_var15, data_out_var14, data_out_var13, data_out_var12};
                  shift_out2_mid     <= {data_out_var11, data_out_var10, data_out_var9, data_out_var8};
                  shift_out1_mid     <= {data_out_var7, data_out_var6, data_out_var5, data_out_var4};     
				  shift_out0_mid     <= {data_out_var3, data_out_var2, data_out_var1, data_out_var0};
               end  
            else
               begin
                  shift_out3_mid     <= shift_out3_mid;
                  shift_out2_mid     <= shift_out2_mid;
                  shift_out1_mid     <= shift_out1_mid;     
				  shift_out0_mid     <= shift_out0_mid;
               end    
        end    


assign	shift_out3	=	shift_out3_mid;
assign	shift_out2	=	shift_out2_mid;
assign	shift_out1	=	shift_out1_mid;
assign	shift_out0	=	shift_out0_mid; 

endmodule

