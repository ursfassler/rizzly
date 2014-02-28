#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static const R_0_1000000 exp[] = {0,1,1,2,3,5,8,13,21,34};

int main(){
  inst__construct();

  for( int i = 0; i < sizeof(exp)/sizeof(exp[0]); i++ ){
    printf( "%i - %i\n", inst_get(i), exp[i] );
    if( inst_get(i) != exp[i] ){
      return -1;
    }
  }
  
  inst__destruct();
  return 0;
}

