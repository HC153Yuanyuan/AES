
`timescale 1ns/1ps

module	RconKey_pipline(				
	input			clk,
	input			rst_n,
	input	[31:0]	Rkey_in, 
    input	[7:0]	Rcon,
    output	[31:0]	Rkey_out
);

wire	[7:0]	word_3;
wire	[7:0]	word_2;
wire	[7:0]	word_1;
wire	[7:0]	word_0;
wire	[7:0]	S_word_3;
wire	[7:0]	S_word_2;
wire	[7:0]	S_word_1;
wire	[7:0]	S_word_0;
wire	[7:0]	RS_word_3;

wire	[7:0]	out3,out2,out1,out0;

 val	word_3	:=	Rkey_in(23:16)
 val	word_2	:=	Rkey_in(15:8)
 val	word_1	:=	Rkey_in(7:0)
 val	word_0	:=	Rkey_in(31:24)

/*GF_Sbox_key	Sbox3	(.Sboxin(word_3),.Sboxout(S_word_3));
GF_Sbox_key	Sbox2	(.Sboxin(word_2),.Sboxout(S_word_2));
GF_Sbox_key	Sbox1	(.Sboxin(word_1),.Sboxout(S_word_1));
GF_Sbox_key	Sbox0	(.Sboxin(word_0),.Sboxout(S_word_0));*/


/* Sbox_Reg reg3(.clk	(clk),	.rst_n	(rst_n),	.Regin	(S_word_3),	.Regout	(out3));
Sbox_Reg reg2(.clk	(clk),	.rst_n	(rst_n),	.Regin	(S_word_2),	.Regout	(out2));
Sbox_Reg reg1(.clk	(clk),	.rst_n	(rst_n),	.Regin	(S_word_1),	.Regout	(out1));
Sbox_Reg reg0(.clk	(clk),	.rst_n	(rst_n),	.Regin	(S_word_0),	.Regout	(out0)); */

/*GF_Sbox_more_pipline sbox3(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_3),	.Sboxout	(S_word_3));	
GF_Sbox_more_pipline sbox2(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_2),	.Sboxout	(S_word_2));
GF_Sbox_more_pipline sbox1(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_1),	.Sboxout	(S_word_1));
GF_Sbox_more_pipline sbox0(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_0),	.Sboxout	(S_word_0)); */

LUT_Sbox sbox3(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_3),	.Sboxout	(S_word_3));	
LUT_Sbox sbox2(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_2),	.Sboxout	(S_word_2));
LUT_Sbox sbox1(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_1),	.Sboxout	(S_word_1));
LUT_Sbox sbox0(.clk(clk),	.rst_n(rst_n),	.Sboxin	(word_0),	.Sboxout	(S_word_0)); 

//assign	RS_word_3	=	out3 ^ Rcon;
  
//assign	Rkey_out	=	{RS_word_3,out2,out1,out0};

assign	RS_word_3	=	S_word_3 ^ Rcon;

assign	Rkey_out	=	{RS_word_3,S_word_2,S_word_1,S_word_0};

endmodule
