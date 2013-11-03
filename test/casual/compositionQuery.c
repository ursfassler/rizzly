#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static R_0_100 i;

R_0_100 inst_out(){
  return i;
}

int main(){
  inst__construct();

  for( i = 0; i <= 100; i++ ){
    R_0_100 recv = inst_in();
    printf( "%3i <> %3i\n", i, recv );
    if( i != recv ){
      return -1;
    }
  }
  inst__destruct();
  return 0;
}

