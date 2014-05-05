program jump;

uses
  inst;

procedure _trap(); cdecl; public;
begin
  writeln( 'Runtime error' );
  halt(-1);
end;

procedure inst_out(); cdecl; public;
begin
  writeln( 'out' );
end;

begin
  writeln( 'start jump' );
  
  writeln( 'construct' );
  inst__construct();

  writeln( 'in 1' );
  inst_inp();

  writeln( 'in 2' );
  inst_inp();

  writeln( 'in 3' );
  inst_inp();

  writeln( 'in 4' );
  inst_inp();

  writeln( 'in 5' );
  inst_inp();

  writeln( 'in 6' );
  inst_inp();

  writeln( 'destruct' );
  inst__destruct();

  writeln( 'end jump' );
end.

