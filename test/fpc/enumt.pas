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
  inst_in( 0 );
  inst_in( 1 );
  inst_in( 2 );
  inst_in( 3 );
  inst_in( 100 );
  inst_in( 255 );
  writeln( 'end enumt' );
  inst__destruct();
end.

