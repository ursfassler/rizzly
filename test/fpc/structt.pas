program structt;

uses
  inst;

procedure inst_out1_foo(p: structt_Point); cdecl; public;
begin
  if (p.x = 1) and (p.y = 2)
    then writeln( 'ok' )
    else writeln( 'error' );
end;

procedure inst_out2_foo(x: uint8_t; y: uint8_t); cdecl; public;
begin
  if (x = 100) and (y = 200)
    then writeln( 'ok' )
    else writeln( 'error' );
end;

var
  p: structt_Point;

begin
  p.x := 100;
  p.y := 200;
  writeln( 'start structt' );
  inst_in1_foo( 1, 2 );
  inst_in2_foo( p );
  writeln( 'end structt' );
end.

