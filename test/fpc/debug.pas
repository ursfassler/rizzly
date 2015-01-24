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

procedure inst__msgRecv(receiver: Array_4_R_0_10; size: R_0_3); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__DebugName(receiver.data[i]) );
    write( ' ' );
  end;
  writeln();
end;

procedure inst__msgSend(sender: Array_4_R_0_10; size: R_0_3); cdecl; public;
var
  i : Integer;
begin
  write( 'debug: ' );
  for i := size-1 downto 0 do begin
    write( inst__DebugName(sender.data[i]) );
    write( ' ' );
  end;
  writeln();
end;

begin
  writeln( 'start debug' );
  
  inst__construct();

  writeln( 'send foo' );
  inst_infoo();

  writeln( 'send bar' );
  inst_inbar();

  writeln( 'send poh' );
  inst_inpoh();

  inst__destruct();
  
  writeln( 'end debug' );
end.

