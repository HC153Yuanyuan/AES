`timescale 1ns/1ps
////////////////////////////////////////////////////////////////////////
 module AES_top(
    input			AES_clk,
    input			AES_rst_n,
	input			AES_en,
	//input 			AES_round_en2,
    input	[127:0]	AES_data_in,
	input	[127:0]	AES_key_in,
	
	output	reg		AES_data_out_valid,
    output	reg     [127:0]	AES_data_out
    
//	output			AES_data_out_complementary_valid,
//    output	[127:0]	AES_data_out_complementary
);
	reg				[127:0]			R_add_origin_key;				//³õÊŒÒì»òÃÜÔ¿£»
	wire            [127:0]        	round_data_out1;				//ÂÖ±ä»»ÊýŸÝÊä³ö£»
	wire            [127:0]        	round_data_out2;
	wire            [127:0]        	round_data_out3;
	wire            [127:0]        	round_data_out4;
	wire            [127:0]        	round_data_out5;
	wire            [127:0]        	round_data_out6;
	wire            [127:0]        	round_data_out7;
	wire            [127:0]        	round_data_out8;
	wire            [127:0]        	round_data_out9;
	wire            [127:0]        	round_data_out10;

////////////////////////////////////////////////////////////////////////
	wire            [127:0]        	AES_round_key1_out;
	wire            [127:0]        	AES_round_key2_out;
	wire            [127:0]        	AES_round_key3_out;
	wire            [127:0]        	AES_round_key4_out;
	wire            [127:0]        	AES_round_key5_out;
	wire            [127:0]        	AES_round_key6_out;
	wire            [127:0]        	AES_round_key7_out;
	wire            [127:0]        	AES_round_key8_out;
	wire            [127:0]        	AES_round_key9_out;
	wire            [127:0]        	AES_round_key10_out;
////////////////////////////////////////////////////////////////////////
   

    reg [127:0] origin_input;
    reg         AES_en_delay;
    always@(posedge AES_clk)
        begin
            if(!AES_rst_n)
               begin
                  origin_input            <= 128'b0;
                  AES_en_delay            <= 1'b0; 
               end   
            else if(AES_en)
               begin
                  origin_input            <= AES_data_in ;
                  AES_en_delay            <= AES_en;
               end  
            else
               begin
                  origin_input            <= origin_input;
                   AES_en_delay            <= 1'b0; 
               end    
        end    
    

always@(posedge AES_clk)
	begin
		if(~AES_rst_n)
			begin
			   R_add_origin_key                 <= 128'b0;
			end   
		else if(AES_en_delay)
			begin
			   R_add_origin_key                 <= origin_input ^ AES_key_in;
			end   
		else
			begin
			   R_add_origin_key                 <= R_add_origin_key;
			end   
	end
//-------------------------------------------------//

wire 	round_en;
//assign  round_en = 1'b1;
  key_expand key_expand_inst(
	.clk				(AES_clk),
	.rst_n				(AES_rst_n),
	.key_en				(AES_en),
	
	.key_in				(AES_key_in),	
	.key1_out			(AES_round_key1_out),
	.key2_out			(AES_round_key2_out),
	.key3_out			(AES_round_key3_out),
	.key4_out			(AES_round_key4_out),
	.key5_out			(AES_round_key5_out),
	.key6_out			(AES_round_key6_out),
	.key7_out			(AES_round_key7_out),
	.key8_out			(AES_round_key8_out),
	.key9_out			(AES_round_key9_out),
	.key10_out			(AES_round_key10_out),
	.round_en			(round_en)
  );
  
//-------------------------------------------------//
//--------the round_transfor module -------//

///////////////////////////µÚ1ÂÖ///////////////////////////////////////// 	
  round_transform1 round_transform1_inst(
	.round_clk		   	(AES_clk			),
    .round_rst_n         	(AES_rst_n          ),
    //.round_en          	(round_en  || AES_round_en2          	),
	 .round_en          	(round_en           	),
    .round_datain      	(R_add_origin_key     	),
	.round_keyin		(AES_round_key1_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out1_valid     	),
    .round_dataout     	(round_data_out1    	)
  );
  
///////////////////////////µÚ2ÂÖ///////////////////////////////////////// 	
    round_transform2 round_transform2_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out1_valid           	),
    .round_datain      (round_data_out1     ),
	.round_keyin		(AES_round_key2_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out2_valid     	),
    .round_dataout     (round_data_out2    )
  );
  
///////////////////////////µÚ3ÂÖ///////////////////////////////////////// 		
    round_transform3 round_transform3_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2          ),	
	.round_en          	(round_data_out2_valid           	),
    .round_datain      (round_data_out2     ),
	.round_keyin		(AES_round_key3_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out3_valid     	),
    .round_dataout     (round_data_out3    )
  );
  
///////////////////////////µÚ4ÂÖ///////////////////////////////////////// 	
    round_transform4 round_transform4_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out3_valid           	),
    .round_datain      (round_data_out3     ),
	.round_keyin		(AES_round_key4_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out4_valid     	),
    .round_dataout     (round_data_out4    )
  );
  
///////////////////////////µÚ5ÂÖ///////////////////////////////////////// 	
    round_transform5 round_transform5_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),
	.round_en          	(round_data_out4_valid           	),
    .round_datain      (round_data_out4     ),
	.round_keyin		(AES_round_key5_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out5_valid     	),
    .round_dataout     (round_data_out5    )
  );
  
///////////////////////////µÚ6ÂÖ///////////////////////////////////////// 	
    round_transform6 round_transform6_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out5_valid           	),
    .round_datain      (round_data_out5     ),
	.round_keyin		(AES_round_key6_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out6_valid     	),
    .round_dataout     (round_data_out6    )
  );
  
///////////////////////////µÚ7ÂÖ///////////////////////////////////////// 	
    round_transform7 round_transform7_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out6_valid           	),
    .round_datain      (round_data_out6     ),
	.round_keyin		(AES_round_key7_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out7_valid     	),
    .round_dataout     (round_data_out7    )
  );
  
///////////////////////////µÚ8ÂÖ///////////////////////////////////////// 	
    round_transform8 round_transform8_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out7_valid           	),
    .round_datain      (round_data_out7     ),
	.round_keyin		(AES_round_key8_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out8_valid     	),
    .round_dataout     (round_data_out8    )
  );
  
///////////////////////////µÚ9ÂÖ///////////////////////////////////////// 	
    round_transform9 round_transform9_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),	
	.round_en          	(round_data_out8_valid           	),
    .round_datain      (round_data_out8     ),
	.round_keyin		(AES_round_key9_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out9_valid     	),
    .round_dataout     (round_data_out9    )
  );
  
///////////////////////////µÚ10ÂÖ///////////////////////////////////////// 	
    round_transform10 round_transform10_inst(
	.round_clk		   (AES_clk			  ),
    .round_rst_n         (AES_rst_n           ),
    //.round_en          (round_en || AES_round_en2           ),   	
	.round_en          	(round_data_out9_valid           	),
    .round_datain      (round_data_out9     ),
	.round_keyin		(AES_round_key10_out	),
	//.round_keyin		(128'h2b7e1516_28aed2a6_abf71588_09cf4f3c	),
	.round_dataout_valid      	(round_data_out10_valid     	),
    .round_dataout     (round_data_out10    )
  );
//////////////////////////end/////////////////////////////
    always@(posedge AES_clk)
        begin
            if(~AES_rst_n)
               begin
                  AES_data_out_valid <= 1'b0;
                  AES_data_out <= 128'b0;
               end
            else if(round_data_out10_valid )
                begin
                  AES_data_out_valid <= 1'b1;
                  AES_data_out <= round_data_out10;
                end
            else
                begin
                  AES_data_out_valid <= AES_data_out_valid;
                  AES_data_out <= AES_data_out;
                end
        end
        
//////////////////////////œáÊø/////////////////////////////

endmodule
