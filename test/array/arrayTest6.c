#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

int main(){
  inst__construct();

  {
    Array_10_R_0_31 in;

    for( uint8_t i = 0; i < 10; i++ ){
      in.data[i] = (i * 17) % 32;
    }
    
    inst_set( in );
  }
    
  for( uint8_t i = 0; i < 10; i++ ){
    R_0_31 exp = (i * 17) % 32;
    R_0_31 val = inst_get(i);
    printf( "%i: %i <> %i\n", i, exp, val );
    if( exp != val ){
      return EXIT_FAILURE;
    }
  }
  
  inst__destruct();
  
  return 0;
}

