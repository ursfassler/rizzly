#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

static R__20_20 value;

R__20_20 inst_out_foo(){
  return value;
}

int main(){
  inst__entry();

  int i;
  for( i = -20; i <= 20; i++ ){
    value = i;
    R__20_20 ret = inst_in_foo();
    printf( "%i <> %i\n", i, ret );
    if( i != ret ){
      return -1;
    }
  }
  return 0;
}

