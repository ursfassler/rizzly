program structt;

uses
  inst;

procedure inst_out1(x: structt_Point); cdecl; public;
begin
  if (x.x = 1) and (x.y = 2)
    then writeln( 'ok' )
    else writeln( 'error' );
end;

procedure inst_out2(x: R_0_1000; y: R_0_1000); cdecl; public;
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

