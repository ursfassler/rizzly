program arr;

uses
  inst;

procedure inst_out_foo(x: Array_3_U_8); cdecl; public;
begin
  if (x[0] = 100) and (x[1] = 200) and (x[2] = 50) 
    then writeln( 'ok' )
    else writeln( 'error' );
end;

var
  x: Array_10_U_8;

begin
  x[0] := 100;
  x[2] := 200;
  x[5] := 50;
  writeln( 'start arr' );
  inst_in_foo(x);
  writeln( 'end arr' );
end.

