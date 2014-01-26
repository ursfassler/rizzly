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
  R_0_200 a[20];
  for( i = 0; i < 20; i++ ){
    a[i] = i * 10;
  }
  for( i = 0; i <= 200; i++ ){
    a[(i * 5) % 20] = (a[i % 10] + a[10 + (i % 10)]) / 2;
    exp = a[i % 20];
    
    value = 0;
    inst_in( i );
    
    printf( "%i: %i <> %i\n", i, exp, value );
    if( exp != value ){
      return -1;
    }
  }
  
  inst__destruct();
  
  return 0;
}
