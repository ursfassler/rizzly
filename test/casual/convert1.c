#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

int main(){
  int i;
  inst__system_construct();

  for( i = 0; i <= 10; i++ ){
    int ret = inst_in_tack( i );
    int ok = ret == i;
    printf( "i: %2i\tret: %2i\n", i, ret );
    if( !ok ){
      return -1;
    }
  }
  
  inst__system_destruct();
  
  return 0;
}

