program enumt;

uses
  inst;

procedure inst_out_foo(x: enumt_Color); cdecl; public;
begin
  writeln( x );
end;

begin
  writeln( 'start enumt' );
  inst_in_foo( 0 );
  inst_in_foo( 1 );
  inst_in_foo( 2 );
  inst_in_foo( 3 );
  inst_in_foo( 100 );
  inst_in_foo( 255 );
  writeln( 'end enumt' );
end.

