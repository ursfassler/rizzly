program transOrder2;

uses
  inst;

procedure _trap(); cdecl; public;
begin
  writeln( 'Runtime error' );
  halt( -1 );
end;

procedure inst_out(val: R_0_10); cdecl; public;
begin
  writeln( val );
end;

begin
  writeln( 'start transOrder2' );
  
  writeln( 'construct' );
  inst__construct();

  writeln( 'tick' );
  inst_inp( 0 );

  writeln( 'destruct' );
  inst__destruct();

  writeln( 'end transOrder2' );
end.

