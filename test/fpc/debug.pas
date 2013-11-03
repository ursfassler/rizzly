program debug;

uses
  inst;

procedure inst_outfoo(); cdecl; public;
begin
  writeln( 'foo' );
end;

procedure inst_outbar(); cdecl; public;
begin
  writeln( 'bar' );
end;

procedure inst_outpoh(); cdecl; public;
begin
  writeln( 'poh' );
end;

procedure inst__msgRecv(receiver: Array_4_U_4; size: uint8_t); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__getSym(receiver[i]) );
    write( ' ' );
  end;
  writeln();
end;

procedure inst__msgSend(sender: Array_4_U_4; size: uint8_t); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__getSym(sender[i]) );
    write( ' ' );
  end;
  writeln();
end;

begin
  inst__construct();
  writeln( 'start debug' );

  writeln( 'send foo' );
  inst_infoo();

  writeln( 'send bar' );
  inst_inbar();

  writeln( 'send poh' );
  inst_inpoh();

  writeln( 'end debug' );
  inst__destruct();
end.

