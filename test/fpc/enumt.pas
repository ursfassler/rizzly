program enumt;

uses
  inst;

procedure inst_out(x: enumt_Color); cdecl; public;
begin
  writeln( x );
end;

begin
  inst__construct();
  writeln( 'start enumt' );
  inst_inp( 0 );
  inst_inp( 1 );
  inst_inp( 2 );
  inst_inp( 3 );
  inst_inp( 100 );
  inst_inp( 255 );
  writeln( 'end enumt' );
  inst__destruct();
end.

