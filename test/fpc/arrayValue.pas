program arrayValue;

uses
  inst;

procedure inst_out(x: R_0_100); cdecl; public;
begin
  writeln( x );
end;

begin
  inst__construct();
  writeln( 'start arrayValue' );
  inst_inp(0);
  inst_inp(1);
  inst_inp(2);
  inst_inp(3);
  inst_inp(4);
  inst_inp(5);
  inst_inp(6);
  inst_inp(7);
  inst_inp(8);
  inst_inp(9);
  writeln( 'end arrayValue' );
  inst__destruct();
end.

