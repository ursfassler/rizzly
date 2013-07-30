program arrayValue;

uses
  inst;

procedure inst_out_foo(x: uint8_t); cdecl; public;
begin
  writeln( x );
end;

begin
  writeln( 'start arrayValue' );
  inst_in_foo(0);
  inst_in_foo(1);
  inst_in_foo(2);
  inst_in_foo(3);
  inst_in_foo(4);
  inst_in_foo(5);
  inst_in_foo(6);
  inst_in_foo(7);
  inst_in_foo(8);
  inst_in_foo(9);
  writeln( 'end arrayValue' );
end.

