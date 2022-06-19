// Generator : SpinalHDL v1.7.0    git head : eca519e78d4e6022e34911ec300a432ed9db8220
// Component : AES_CORE

`timescale 1ns/1ps 
module AES_CORE (
  input               io_cmd_valid,
  output              io_cmd_ready,
  input      [127:0]  io_cmd_payload_key,
  input      [127:0]  io_cmd_payload_block,
  input               io_cmd_payload_enc,
  output              io_rsp_valid,
  output     [127:0]  io_rsp_payload_block,
  input               clk,
  input               reset
);

  wire                engine_io_engine_cmd_ready;
  wire                engine_io_engine_rsp_valid;
  wire       [127:0]  engine_io_engine_rsp_payload_block;

  AES_ENGINE engine (
    .io_engine_cmd_valid         (io_cmd_valid                             ), //i
    .io_engine_cmd_ready         (engine_io_engine_cmd_ready               ), //o
    .io_engine_cmd_payload_key   (io_cmd_payload_key[127:0]                ), //i
    .io_engine_cmd_payload_block (io_cmd_payload_block[127:0]              ), //i
    .io_engine_cmd_payload_enc   (io_cmd_payload_enc                       ), //i
    .io_engine_rsp_valid         (engine_io_engine_rsp_valid               ), //o
    .io_engine_rsp_payload_block (engine_io_engine_rsp_payload_block[127:0]), //o
    .clk                         (clk                                      ), //i
    .reset                       (reset                                    )  //i
  );
  assign io_cmd_ready = engine_io_engine_cmd_ready;
  assign io_rsp_valid = engine_io_engine_rsp_valid;
  assign io_rsp_payload_block = engine_io_engine_rsp_payload_block;

endmodule
