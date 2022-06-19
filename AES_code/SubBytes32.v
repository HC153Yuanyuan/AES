
`timescale 1ns/1ps

module	SubBytes32(
	input			clk,
	input			rst_n,
	input			subBytes_en,
	input	[31:0]	subBytes_in,
	output	[31:0]	subBytes_out
);

reg     [7:0]	aux3_mid;
reg     [7:0]	aux2_mid;
reg     [7:0]	aux1_mid;
reg     [7:0]	aux0_mid;
    always@(posedge clk)
        begin
            if(!rst_n)
               begin
                  aux3_mid     <= 8'b0;
				  aux2_mid     <= 8'b0;
				  aux1_mid     <= 8'b0;
				  aux0_mid     <= 8'b0;
               end   
            else if(subBytes_en)
               begin
                  aux3_mid     <= subBytes_in[31:24];
				  aux2_mid     <= subBytes_in[23:16];
				  aux1_mid     <= subBytes_in[15:8];
				  aux0_mid     <= subBytes_in[7:0];
               end  
            else
               begin
                  aux3_mid     <= aux3_mid;
				  aux2_mid     <= aux2_mid;
				  aux1_mid     <= aux1_mid;
				  aux0_mid     <= aux0_mid;
               end    
        end   


wire	[7:0]	aux3;
wire	[7:0]	aux2;
wire	[7:0]	aux1;
wire	[7:0]	aux0;

assign  aux3	=   aux3_mid;
assign  aux2	=   aux2_mid;
assign  aux1	=   aux1_mid;
assign  aux0	=   aux0_mid;

//Sbox output
wire	[7:0]	outx3,outx2,outx1,outx0;

//wire	[7:0]	out3,out2,out1,out0;	

reg subBytes_en_delay1;
reg subBytes_en_delay2;
reg subBytes_en_delay3;

    always@(posedge clk)
        begin
            if(!rst_n)
               begin
                  subBytes_en_delay1     <= 1'b0;
				  subBytes_en_delay2     <= 1'b0;
				  subBytes_en_delay3     <= 1'b0;
               end   
            else if(subBytes_en)
               begin
                  subBytes_en_delay1     <= subBytes_en;
				  subBytes_en_delay2     <= subBytes_en_delay1;
				  subBytes_en_delay3     <= subBytes_en_delay2;
               end  
            else
               begin
                  subBytes_en_delay1     <= 1'b0;
				  subBytes_en_delay2     <= 1'b0;
				  subBytes_en_delay3     <= 1'b0;
               end    
        end   


LUT_Sbox sbox3(.clk(clk),	.rst_n(rst_n),	 .Sboxin	(aux3),	.Sboxout	(outx3));	
LUT_Sbox sbox2(.clk(clk),	.rst_n(rst_n),	 .Sboxin	(aux2),	.Sboxout	(outx2));
LUT_Sbox sbox1(.clk(clk),	.rst_n(rst_n),	 .Sboxin	(aux1),	.Sboxout	(outx1));
LUT_Sbox sbox0(.clk(clk),	.rst_n(rst_n),	 .Sboxin	(aux0),	.Sboxout	(outx0));

/* GF_Sbox_no_pipline sbox3(.Sboxin	(aux3),	.Sboxout	(outx3));	
GF_Sbox_no_pipline sbox2(.Sboxin	(aux2),	.Sboxout	(outx2));
GF_Sbox_no_pipline sbox1(.Sboxin	(aux1),	.Sboxout	(outx1));
GF_Sbox_no_pipline sbox0(.Sboxin	(aux0),	.Sboxout	(outx0)); */

/* Sbox_Reg reg3(.clk	(clk),	.rst	(rst_n),	.Regin	(outx3),	.Regout	(out3));
Sbox_Reg reg2(.clk	(clk),	.rst	(rst_n),	.Regin	(outx2),	.Regout	(out2));
Sbox_Reg reg1(.clk	(clk),	.rst	(rst_n),	.Regin	(outx1),	.Regout	(out1));
Sbox_Reg reg0(.clk	(clk),	.rst	(rst_n),	.Regin	(outx0),	.Regout	(out0)); */

//assign	subBytes_out	=	{out3,out2,out1,out0};

reg [31:0]	subBytes_out_mid;
    always@(posedge clk)
        begin
            if(!rst_n)
               begin
                  subBytes_out_mid     <= 32'b0;
               end   
            else if(subBytes_en_delay3)
               begin
                  subBytes_out_mid     <= {outx3,outx2,outx1,outx0};
               end  
            else
               begin
                  subBytes_out_mid     <= subBytes_out_mid;
               end    
        end   
		
assign	subBytes_out	=	subBytes_out_mid;
  

endmodule
