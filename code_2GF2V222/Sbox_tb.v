`timescale 1ns/1ps
//////////////////////////////////////////////////////////////////////////////////

module Sbox_tb();

reg		[7:0]	Sboxin=8'h0;
reg		clk=0;
reg		rst=1;

wire	[7:0]	Sboxout;

GF2V222BasedSBox	Sboxtb(
	.Sin		(Sboxin),	
	.Sout	(Sboxout)
);

initial
    begin
	$dumpfile("AES_Sbox_GF2V222.dump");
	//$dumpfile("../output/VCS/code_2GF2V222/AES_Sbox_GF2V222.dump");
	//$dumpfile("AES_32_without_resistance_with_LUT_Sbox_for_netlist.dump");

	$dumpvars();
    end

always #20 clk = ~clk;

always @(posedge clk or negedge rst)
	begin
		if(~rst)
			Sboxin=0;
		else
			Sboxin=Sboxin+1;
	end

initial 
	begin
		#80 rst=0;
		
		#80 rst=1;  
	
		#20000	$finish;
	end


endmodule
