#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static R_0_20 value;

void inst_out_tick(R_0_20 x){
  value = x;
}

int main(){
  inst__system_construct();

  R_0_20 i;
  R_0_20 exp;
  for( i = 0; i <= 20; i++ ){
    if( i < 0 ){
      return -1;
    } else if( i < 9 ){
      exp = 2;
    } else if( i < 10 ){
      exp = 3;
    } else if( i <= 20 ){
      exp = 4;
    } else {
      return -1;
    }
    inst_in_tick( i );
    printf( "%i: %i <> %i\n", i, exp, value );
    if( exp != value ){
      return -1;
    }
  }
  return 0;
}

