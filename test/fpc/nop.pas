program nop;

uses
  inst;

procedure inst_out(); cdecl; public;
begin
  writeln( 'ok' );
end;

begin
  inst__construct();
  writeln( 'start nop' );
  inst_in();
  writeln( 'end nop' );
  inst__destruct();
end.

