#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

static R__20_20 value;

R__20_20 inst_out(){
  return value;
}

int main(){
  inst__construct();

  int i;
  for( i = -20; i <= 20; i++ ){
    value = i;
    R__20_20 ret = inst_ind();
    printf( "%i <> %i\n", i, ret );
    if( i != ret ){
      return -1;
    }
  }
  inst__destruct();
  return 0;
}

