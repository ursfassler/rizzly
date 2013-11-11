program arr;

uses
  inst;

procedure inst_out(x: Array_3_R_0_100); cdecl; public;
begin
  if (x[0] = 100) and (x[1] = 200) and (x[2] = 50) 
    then writeln( 'ok' )
    else writeln( 'error' );
end;

var
  x: Array_10_R_0_100;

begin
  inst__construct();
  x[0] := 100;
  x[2] := 200;
  x[5] := 50;
  writeln( 'start arr' );
  inst_in(x);
  writeln( 'end arr' );
  inst__destruct();
end.

