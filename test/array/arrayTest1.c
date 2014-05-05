#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static R_0_200 value;

void inst_out(R_0_200 x){
  value = x;
}

int main(){
  inst__construct();

  R_0_200 i;
  R_0_200 exp;

  for( i = 0; i <= 200; i++ ){
    exp = (i % 20) * 10;
    value = 200;
    
    inst_inp( i );
    
    printf( "%i: %i <> %i\n", i, exp, value );
    if( exp != value ){
      return -1;
    }
  }
  
  inst__destruct();
  
  return 0;
}

