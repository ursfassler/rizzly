program structt;

uses
  inst;

procedure inst_out1(p: structt_Point); cdecl; public;
begin
  if (p.x = 1) and (p.y = 2)
    then writeln( 'ok' )
    else writeln( 'error' );
end;

procedure inst_out2(x: uint8_t; y: uint8_t); cdecl; public;
begin
  if (x = 100) and (y = 200)
    then writeln( 'ok' )
    else writeln( 'error' );
end;

var
  p: structt_Point;

begin
  inst__construct();
  p.x := 100;
  p.y := 200;
  writeln( 'start structt' );
  inst_in1( 1, 2 );
  inst_in2( p );
  writeln( 'end structt' );
  inst__destruct();
end.

