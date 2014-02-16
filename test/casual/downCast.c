#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  <stdlib.h>
#include  "output/inst.h"

void check( R_0_200 e, R_0_200 r, const char *s ){
  if( e != r ){ 
    printf( "%s: %i <> %i\n", s, e, r );
    exit(-1);
  }
}

int main(){
  inst__construct();

  R_0_50000 i;
  R_0_200 exp;
  R_0_200 ret;
  for( i = 0; i <= 50000; i++ ){
    exp = i & 127;
    ret = inst_a( i );
    check( exp, ret, "a" );
    
    exp = i % 201;
    ret = inst_b( i );
    check( exp, ret, "b" );
    
    exp = i & 31;
    ret = inst_c( i );
    check( exp, ret, "c" );
    
    exp = i % 42;
    ret = inst_d( i );
    check( exp, ret, "d" );
  }
  
  inst__destruct();
  
  return 0;
}

