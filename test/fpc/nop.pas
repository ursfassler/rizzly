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
  inst_inp();
  writeln( 'end nop' );
  inst__destruct();
end.

