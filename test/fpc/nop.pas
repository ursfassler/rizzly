program nop;

uses
  inst;

procedure inst_out_foo(); cdecl; public;
begin
  writeln( 'ok' );
end;

begin
  writeln( 'start nop' );
  inst_in_foo();
  writeln( 'end nop' );
end.

