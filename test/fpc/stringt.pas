program stringt;

uses
  inst;

procedure inst_out1(value: String_); cdecl; public;
begin
  write( '1:' );
  writeln( value );
end;

procedure inst_out2(value: String_); cdecl; public;
begin
  write( '2:' );
  writeln( value );
end;

begin
  inst__construct();
  writeln( 'start stringt' );
  inst_in( 'Hello' );
  inst_in( 'World' );
  writeln( 'end stringt' );
  inst__destruct();
end.

