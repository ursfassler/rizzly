#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static Array_10_R_0_31 out;

void inst_out(Array_10_R_0_31 x){
  out = x;
}

int main(){
  inst__construct();

  uint8_t i;
  Array_10_R_0_31 in;

  for( i = 0; i < 10; i++ ){
    in.data[i] = (i * 17) % 32;
  }
    
  inst_inp( in );
  
  for( i = 0; i < 10; i++ ){
    in.data[i] = 0;
  }
  
  for( i = 0; i < 10; i++ ){
    R_0_31 exp = (i * 17) % 32;
    printf( "%i: %i <> %i\n", i, exp, out.data[i] );
    if( exp != out.data[i] ){
      return EXIT_FAILURE;
    }
  }
  
  inst__destruct();
  
  return 0;
}

