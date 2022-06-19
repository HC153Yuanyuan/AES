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

//wire				AES_data_out_complementary_valid;
//wire	[127:0]		AES_data_out_complementary;
  
AES_top AES_topmodule(
    .AES_clk					(AES_clk				),
    .AES_rst_n					(AES_rst_n				),
    .AES_en					(AES_en					),
    .AES_data_in				(AES_data_in				),
    .AES_key_in					(AES_key_in				),
    .AES_data_out				(AES_data_out				),
    .AES_data_out_valid			        (AES_data_out_valid			)   
//    .AES_data_out_complementary_valid           (AES_data_out_complementary_valid	),
//    .AES_data_out_complementary			(AES_data_out_complementary		)
);

///////////////////////////generate vcd file/////////////////////
 initial
  begin


	$dumpfile("AES_code_vcd_topmodule_0.vcd");
	$dumpvars(0,AES_top_tb.AES_topmodule);


  end

////////////////////////read sdf file//////////////////////////////////////////

  initial
    begin
	$sdf_annotate("../output/DC/AES_code/AES_code_0.sdf",AES_topmodule);
    end

////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////

  always #5 AES_clk = ~AES_clk;
  
  initial begin

    #20 AES_rst_n = 0;
    #10 AES_rst_n = 1;
	#41 begin
		AES_en=1;
		AES_data_in = 128'h00000056_00000000_00000000_00000000;
		AES_key_in  = 128'haa2bdb40_bff6a5e8_caa9ba3e_bc1e2acc; 

        #510    AES_en=0;
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
