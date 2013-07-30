program debug;

uses
  inst;

procedure inst_out_foo(); cdecl; public;
begin
  writeln( 'foo' );
end;

procedure inst_out_bar(); cdecl; public;
begin
  writeln( 'bar' );
end;

procedure inst_out_poh(); cdecl; public;
begin
  writeln( 'poh' );
end;

procedure inst__debug_msgRecv(receiver: Array_4_U_4; size: uint8_t); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__debugQuery_getSym(receiver[i]) );
    write( ' ' );
  end;
  writeln();
end;

procedure inst__debug_msgSend(sender: Array_4_U_4; size: uint8_t); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__debugQuery_getSym(sender[i]) );
    write( ' ' );
  end;
  writeln();
end;

begin
  writeln( 'start debug' );

  writeln( 'send foo' );
  inst_in_foo();

  writeln( 'send bar' );
  inst_in_bar();

  writeln( 'send poh' );
  inst_in_poh();

  writeln( 'end debug' );
end.

