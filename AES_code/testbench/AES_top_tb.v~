//***********************************//
//
//**********************************//

  `timescale 1ns/1ps

module AES_top_tb();

reg		  			AES_clk = 0;
reg					AES_rst_n = 0;
reg					AES_en = 0;
reg		[127:0]		AES_data_in = 128'b0;
reg		[127:0]		AES_key_in = 128'b0;
//reg					AES_round_en2 = 0;

wire                AES_data_out_valid;
wire	[127:0]		AES_data_out;

wire				AES_data_out_complementary_valid;
wire	[127:0]		AES_data_out_complementary;
//wire				AES_end;
  
AES_top AES_top_inst(
    .AES_clk(AES_clk),
    .AES_rst_n(AES_rst_n),
	.AES_en(AES_en),
    .AES_data_in(AES_data_in),
	.AES_key_in(AES_key_in),
//	.AES_round_en2(AES_round_en2),
    .AES_data_out(AES_data_out),
	.AES_data_out_valid(AES_data_out_valid),
    
	.AES_data_out_complementary_valid(AES_data_out_complementary_valid),
    .AES_data_out_complementary(AES_data_out_complementary)
    
//    .AES_end(AES_end)

);

//report wave form. generate verilog.dump for dve&
  initial
    begin
	//$dumpfile("Masked_AES_32bit_with_GF_Sbox_NP_with_CSE_112.dump");
	$dumpfile("../output/VCS/complementary_full_expansion/AES_complementary_0.dump");

	$dumpvars();
    end

///////////////////////////generate vcd file/////////////////////

  always #5 AES_clk = ~AES_clk;
  
  initial begin

    #20 AES_rst_n = 0;
    #10 AES_rst_n = 1;
	#41 begin
		AES_en=1;
		AES_data_in = 128'hd7b26248_e8351227_5573a1e5_e8f263b3;
		AES_key_in = 128'haa2bdb40_bff6a5e8_caa9ba3e_bc1e2acc; //2b7e1516_28aed2a6_abf71588_09cf4f3c;//3925841d_02dc09fb_dc118597_196a0b32            //aa2bdb40_bff6a5e8_caa9ba3e_bc1e2acc;//55d424bf_40095a17_355645c1_43e1d533;
		end

	#150               
	    begin            
		AES_data_in = 128'ha6f2daeb_140fa720_529e75d5_21cbc681;
		end		
    #10
        begin
        AES_data_in = 128'hd7b26248_e8351227_5573a1e5_e8f263b3;          
        end	
    #10
        begin
        AES_data_in = 128'hf301a68a_9e9ffa50_844581d9_e290d818;
        end            
		
		
        #3000 $finish;
	end
	
endmodule
