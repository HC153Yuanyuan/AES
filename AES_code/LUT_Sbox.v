`timescale 1ns / 1ps
////////////////////////////////////////////////////////////////////////
module LUT_Sbox(
  input   [7:0]   Sboxin,
  input			  clk,
  input		      rst_n,
  output  [7:0]   Sboxout
);
  
  reg     [7:0]   outx;
always @(posedge clk)
  begin
    if (~rst_n)
	  begin  
		outx <= 8'h00; 
	  end
	else                         //if (Sbox_en)
    begin	
    case (Sboxin)
    8'h00:    outx <= 8'h63;       
    8'h01:    outx <= 8'h7c; 
    8'h02:    outx <= 8'h77; 
    8'h03:    outx<=8'h7b; 
    8'h04:    outx<=8'hf2; 
    8'h05:    outx<=8'h6b; 
    8'h06:    outx<=8'h6f; 
    8'h07:    outx<=8'hc5; 
    8'h08:    outx<=8'h30; 
    8'h09:    outx<=8'h01; 
    8'h0a:    outx<=8'h67; 
    8'h0b:    outx<=8'h2b; 
    8'h0c:    outx<=8'hfe; 
    8'h0d:    outx<=8'hd7; 
    8'h0e:    outx<=8'hab; 
    8'h0f:    outx<=8'h76; 

    8'h10:    outx<=8'hca;       
    8'h11:    outx<=8'h82; 
    8'h12:    outx<=8'hc9; 
    8'h13:    outx<=8'h7d; 
    8'h14:    outx<=8'hfa; 
    8'h15:    outx<=8'h59; 
    8'h16:    outx<=8'h47; 
    8'h17:    outx<=8'hf0; 
    8'h18:    outx<=8'had; 
    8'h19:    outx<=8'hd4; 
    8'h1a:    outx<=8'ha2; 
    8'h1b:    outx<=8'haf; 
    8'h1c:    outx<=8'h9c; 
    8'h1d:    outx<=8'ha4; 
    8'h1e:    outx<=8'h72; 
    8'h1f:    outx<=8'hc0; 

    8'h20:    outx<=8'hb7;       
    8'h21:    outx<=8'hfd; 
    8'h22:    outx<=8'h93; 
    8'h23:    outx<=8'h26; 
    8'h24:    outx<=8'h36; 
    8'h25:    outx<=8'h3f; 
    8'h26:    outx<=8'hf7; 
    8'h27:    outx<=8'hcc; 
    8'h28:    outx<=8'h34; 
    8'h29:    outx<=8'ha5; 
    8'h2a:    outx<=8'he5; 
    8'h2b:    outx<=8'hf1; 
    8'h2c:    outx<=8'h71; 
    8'h2d:    outx<=8'hd8; 
    8'h2e:    outx<=8'h31; 
    8'h2f:    outx<=8'h15; 

    8'h30:    outx<=8'h04;       
    8'h31:    outx<=8'hc7; 
    8'h32:    outx<=8'h23; 
    8'h33:    outx<=8'hc3; 
    8'h34:    outx<=8'h18; 
    8'h35:    outx<=8'h96; 
    8'h36:    outx<=8'h05; 
    8'h37:    outx<=8'h9a; 
    8'h38:    outx<=8'h07; 
    8'h39:    outx<=8'h12; 
    8'h3a:    outx<=8'h80; 
    8'h3b:    outx<=8'he2; 
    8'h3c:    outx<=8'heb; 
    8'h3d:    outx<=8'h27; 
    8'h3e:    outx<=8'hb2; 
    8'h3f:    outx<=8'h75; 

    8'h40:    outx<=8'h09;       
    8'h41:    outx<=8'h83; 
    8'h42:    outx<=8'h2c; 
    8'h43:    outx<=8'h1a; 
    8'h44:    outx<=8'h1b; 
    8'h45:    outx<=8'h6e; 
    8'h46:    outx<=8'h5a; 
    8'h47:    outx<=8'ha0; 
    8'h48:    outx<=8'h52; 
    8'h49:    outx<=8'h3b; 
    8'h4a:    outx<=8'hd6; 
    8'h4b:    outx<=8'hb3; 
    8'h4c:    outx<=8'h29; 
    8'h4d:    outx<=8'he3; 
    8'h4e:    outx<=8'h2f; 
    8'h4f:    outx<=8'h84; 

    8'h50:    outx<=8'h53;       
    8'h51:    outx<=8'hd1; 
    8'h52:    outx<=8'h00; 
    8'h53:    outx<=8'hed; 
    8'h54:    outx<=8'h20; 
    8'h55:    outx<=8'hfc; 
    8'h56:    outx<=8'hb1; 
    8'h57:    outx<=8'h5b; 
    8'h58:    outx<=8'h6a; 
    8'h59:    outx<=8'hcb; 
    8'h5a:    outx<=8'hbe; 
    8'h5b:    outx<=8'h39; 
    8'h5c:    outx<=8'h4a; 
    8'h5d:    outx<=8'h4c; 
    8'h5e:    outx<=8'h58; 
    8'h5f:    outx<=8'hcf; 

    8'h60:    outx<=8'hd0;       
    8'h61:    outx<=8'hef; 
    8'h62:    outx<=8'haa; 
    8'h63:    outx<=8'hfb; 
    8'h64:    outx<=8'h43; 
    8'h65:    outx<=8'h4d; 
    8'h66:    outx<=8'h33; 
    8'h67:    outx<=8'h85; 
    8'h68:    outx<=8'h45; 
    8'h69:    outx<=8'hf9; 
    8'h6a:    outx<=8'h02; 
    8'h6b:    outx<=8'h7f; 
    8'h6c:    outx<=8'h50; 
    8'h6d:    outx<=8'h3c; 
    8'h6e:    outx<=8'h9f; 
    8'h6f:    outx<=8'ha8; 
    
    8'h70:    outx<=8'h51;       
    8'h71:    outx<=8'ha3; 
    8'h72:    outx<=8'h40; 
    8'h73:    outx<=8'h8f; 
    8'h74:    outx<=8'h92; 
    8'h75:    outx<=8'h9d; 
    8'h76:    outx<=8'h38; 
    8'h77:    outx<=8'hf5; 
    8'h78:    outx<=8'hbc; 
    8'h79:    outx<=8'hb6; 
    8'h7a:    outx<=8'hda; 
    8'h7b:    outx<=8'h21; 
    8'h7c:    outx<=8'h10; 
    8'h7d:    outx<=8'hff; 
    8'h7e:    outx<=8'hf3; 
    8'h7f:    outx<=8'hd2;    
    
    8'h80:    outx<=8'hcd;       
    8'h81:    outx<=8'h0c; 
    8'h82:    outx<=8'h13; 
    8'h83:    outx<=8'hec; 
    8'h84:    outx<=8'h5f; 
    8'h85:    outx<=8'h97; 
    8'h86:    outx<=8'h44; 
    8'h87:    outx<=8'h17; 
    8'h88:    outx<=8'hc4; 
    8'h89:    outx<=8'ha7; 
    8'h8a:    outx<=8'h7e; 
    8'h8b:    outx<=8'h3d; 
    8'h8c:    outx<=8'h64; 
    8'h8d:    outx<=8'h5d; 
    8'h8e:    outx<=8'h19; 
    8'h8f:    outx<=8'h73; 
    
    8'h90:    outx<=8'h60;       
    8'h91:    outx<=8'h81; 
    8'h92:    outx<=8'h4f; 
    8'h93:    outx<=8'hdc; 
    8'h94:    outx<=8'h22; 
    8'h95:    outx<=8'h2a; 
    8'h96:    outx<=8'h90; 
    8'h97:    outx<=8'h88; 
    8'h98:    outx<=8'h46; 
    8'h99:    outx<=8'hee; 
    8'h9a:    outx<=8'hb8; 
    8'h9b:    outx<=8'h14; 
    8'h9c:    outx<=8'hde; 
    8'h9d:    outx<=8'h5e; 
    8'h9e:    outx<=8'h0b; 
    8'h9f:    outx<=8'hdb; 
    
    8'ha0:    outx<=8'he0;       
    8'ha1:    outx<=8'h32; 
    8'ha2:    outx<=8'h3a; 
    8'ha3:    outx<=8'h0a; 
    8'ha4:    outx<=8'h49; 
    8'ha5:    outx<=8'h06; 
    8'ha6:    outx<=8'h24; 
    8'ha7:    outx<=8'h5c; 
    8'ha8:    outx<=8'hc2; 
    8'ha9:    outx<=8'hd3; 
    8'haa:    outx<=8'hac; 
    8'hab:    outx<=8'h62; 
    8'hac:    outx<=8'h91; 
    8'had:    outx<=8'h95; 
    8'hae:    outx<=8'he4; 
    8'haf:    outx<=8'h79; 
    
    8'hb0:    outx<=8'he7;       
    8'hb1:    outx<=8'hc8; 
    8'hb2:    outx<=8'h37; 
    8'hb3:    outx<=8'h6d; 
    8'hb4:    outx<=8'h8d; 
    8'hb5:    outx<=8'hd5; 
    8'hb6:    outx<=8'h4e; 
    8'hb7:    outx<=8'ha9; 
    8'hb8:    outx<=8'h6c; 
    8'hb9:    outx<=8'h56; 
    8'hba:    outx<=8'hf4; 
    8'hbb:    outx<=8'hea; 
    8'hbc:    outx<=8'h65; 
    8'hbd:    outx<=8'h7a; 
    8'hbe:    outx<=8'hae; 
    8'hbf:    outx<=8'h08; 
    
    8'hc0:    outx<=8'hba;       
    8'hc1:    outx<=8'h78; 
    8'hc2:    outx<=8'h25; 
    8'hc3:    outx<=8'h2e; 
    8'hc4:    outx<=8'h1c; 
    8'hc5:    outx<=8'ha6; 
    8'hc6:    outx<=8'hb4; 
    8'hc7:    outx<=8'hc6; 
    8'hc8:    outx<=8'he8; 
    8'hc9:    outx<=8'hdd; 
    8'hca:    outx<=8'h74; 
    8'hcb:    outx<=8'h1f; 
    8'hcc:    outx<=8'h4b; 
    8'hcd:    outx<=8'hbd; 
    8'hce:    outx<=8'h8b; 
    8'hcf:    outx<=8'h8a; 
    
    8'hd0:    outx<=8'h70;       
    8'hd1:    outx<=8'h3e; 
    8'hd2:    outx<=8'hb5; 
    8'hd3:    outx<=8'h66; 
    8'hd4:    outx<=8'h48; 
    8'hd5:    outx<=8'h03; 
    8'hd6:    outx<=8'hf6; 
    8'hd7:    outx<=8'h0e; 
    8'hd8:    outx<=8'h61; 
    8'hd9:    outx<=8'h35; 
    8'hda:    outx<=8'h57; 
    8'hdb:    outx<=8'hb9; 
    8'hdc:    outx<=8'h86; 
    8'hdd:    outx<=8'hc1; 
    8'hde:    outx<=8'h1d; 
    8'hdf:    outx<=8'h9e; 
    
    8'he0:    outx<=8'he1;       
    8'he1:    outx<=8'hf8; 
    8'he2:    outx<=8'h98; 
    8'he3:    outx<=8'h11; 
    8'he4:    outx<=8'h69; 
    8'he5:    outx<=8'hd9; 
    8'he6:    outx<=8'h8e; 
    8'he7:    outx<=8'h94; 
    8'he8:    outx<=8'h9b; 
    8'he9:    outx<=8'h1e; 
    8'hea:    outx<=8'h87; 
    8'heb:    outx<=8'he9; 
    8'hec:    outx<=8'hce; 
    8'hed:    outx<=8'h55; 
    8'hee:    outx<=8'h28; 
    8'hef:    outx<=8'hdf; 
    
    8'hf0:    outx<=8'h8c;       
    8'hf1:    outx<=8'ha1; 
    8'hf2:    outx<=8'h89; 
    8'hf3:    outx<=8'h0d; 
    8'hf4:    outx<=8'hbf; 
    8'hf5:    outx<=8'he6; 
    8'hf6:    outx<=8'h42; 
    8'hf7:    outx<=8'h68; 
    8'hf8:    outx<=8'h41; 
    8'hf9:    outx<=8'h99; 
    8'hfa:    outx<=8'h2d; 
    8'hfb:    outx<=8'h0f; 
    8'hfc:    outx<=8'hb0; 
    8'hfd:    outx<=8'h54; 
    8'hfe:    outx<=8'hbb; 
    8'hff:    outx<=8'h16;
    default:  outx<=8'h00;
  endcase
  end
 /* else
    begin
      outx<=  outx;
    end  */
end
assign  Sboxout = outx;
endmodule
