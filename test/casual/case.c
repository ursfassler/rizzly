#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static R_0_100 value;

void inst_out_tick(R_0_100 x){
  value = x;
}

int main(){
  inst__system_construct();

  R_0_100 i;
  R_0_100 exp;
  for( i = 0; i <= 100; i++ ){
    if( i == 0 ){
      exp = 2;
    } else if( i == 1 ){
      exp = 0;
    } else if( ((i >= 3) && (i <=10)) || (i == 12) ){
      exp = 1;
    } else if( (i == 2) || (i == 11) || (i == 20) ){
      exp = 10;
    } else if( ((i >= 30) && (i <=40)) || (i == 15) ){
      exp = 12;
    } else {
      exp = 8;
    }
    inst_in_tick( i );
    printf( "%i: %i <> %i\n", i, exp, value );
    if( exp != value ){
      return -1;
    }
  }
  return 0;
}

