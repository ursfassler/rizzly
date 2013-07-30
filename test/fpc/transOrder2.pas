program transOrder2;

uses
  inst;

procedure inst_out_tick(val: uint8_t); cdecl; public;
begin
  writeln( val );
end;

begin
  writeln( 'start transOrder2' );
  
  writeln( 'construct' );
  inst__system_construct();

  writeln( 'tick' );
  inst_in_tick( 0 );

  writeln( 'destruct' );
  inst__system_destruct();

  writeln( 'end transOrder2' );
end.

