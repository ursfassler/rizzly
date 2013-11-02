#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static R_0_200 value;

void inst_out_tick(R_0_200 x){
  value = x;
}

int main(){
  inst__system_construct();

  R_0_200 i;
  R_0_200 exp;

  for( i = 0; i <= 200; i++ ){
    exp = (i % 20) * 10;
    value = 200;
    
    inst_in_tick( i );
    
    printf( "%i: %i <> %i\n", i, exp, value );
    if( exp != value ){
      return -1;
    }
  }
  return 0;
}

