#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

int main(){
  inst__construct();

  Array_10_R_0_100 in, out;

  for( uint8_t i = 0; i < 10; i++ ){
    in.data[i] = (9-i) * (9-i);
  }
    
  out = inst_data( in );
    
  for( uint8_t i = 0; i < 10; i++ ){
    R_0_100 exp = i*i;
    printf( "%i: %i <> %i\n", i, exp, out.data[i] );
    if( exp != out.data[i] ){
      return EXIT_FAILURE;
    }
  }
  
  inst__destruct();
  
  return 0;
}

