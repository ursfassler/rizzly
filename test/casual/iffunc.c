#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  int i;
  inst__system_construct();

  for( i = 0; i <= 10; i++ ){
    int ret = inst_in_tick( i );
    int ok = ret == (i > 5 ? 0 : 10);
    printf( "i: %2i\tret: %2i\n", i, ret );
    if( !ok ){
      return -1;
    }
  }
  return 0;
}

