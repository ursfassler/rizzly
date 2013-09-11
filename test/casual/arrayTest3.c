#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__entry();
  
  R_0_31 i;
  for( i = 0; i < 32; i++ ){
    R_0_31 v = inst_in_tick( i );
    
    printf( "%i <> %i\n", i, v );
    if( i != v ){
      return -1;
    }
  }
  return 0;
}

