#include  <stdint.h>
#include  <stdio.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  for( R_0_15 i = 0; i < 16; i++ ){
    R_0_15 exp = (~i) & 0xf;
    R_0_15 val = inst_invert( i );
    printf( "%i <> %i\n", exp, val );
    if( exp != val ){
      return -1;
    }
  }
    
  inst__destruct();

  return 0;
}

