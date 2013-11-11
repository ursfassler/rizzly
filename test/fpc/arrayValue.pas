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
  inst_in(0);
  inst_in(1);
  inst_in(2);
  inst_in(3);
  inst_in(4);
  inst_in(5);
  inst_in(6);
  inst_in(7);
  inst_in(8);
  inst_in(9);
  writeln( 'end arrayValue' );
  inst__destruct();
end.

