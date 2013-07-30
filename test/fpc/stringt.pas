program stringt;

uses
  inst;

procedure inst_out1_foo(value: string_t); cdecl; public;
begin
  write( '1:' );
  writeln( value );
end;

procedure inst_out2_foo(value: string_t); cdecl; public;
begin
  write( '2:' );
  writeln( value );
end;

begin
  writeln( 'start stringt' );
  inst_in_foo( 'Hello' );
  inst_in_foo( 'World' );
  writeln( 'end stringt' );
end.

