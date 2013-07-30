program jump;

uses
  inst;

procedure inst_out_tick(); cdecl; public;
begin
  writeln( 'out' );
end;

begin
  writeln( 'start jump' );
  
  writeln( 'construct' );
  inst__system_construct();

  writeln( 'in 1' );
  inst_in_tick();

  writeln( 'in 2' );
  inst_in_tick();

  writeln( 'in 3' );
  inst_in_tick();

  writeln( 'in 4' );
  inst_in_tick();

  writeln( 'in 5' );
  inst_in_tick();

  writeln( 'in 6' );
  inst_in_tick();

  writeln( 'destruct' );
  inst__system_destruct();

  writeln( 'end jump' );
end.

