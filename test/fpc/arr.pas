program arr;

uses
  inst;

procedure inst_out(x: Array_3_R_0_100); cdecl; public;
begin
  if (x.data[0] = 100) and (x.data[1] = 200) and (x.data[2] = 50) 
    then writeln( 'ok' )
    else writeln( 'error' );
end;

var
  x: Array_10_R_0_100;

begin
  inst__construct();
  x.data[0] := 100;
  x.data[2] := 200;
  x.data[5] := 50;
  writeln( 'start arr' );
  inst_inp(x);
  writeln( 'end arr' );
  inst__destruct();
end.

