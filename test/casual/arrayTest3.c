#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

int main(){
  inst__system_construct();
  
  R_0_31 i;
  for( i = 0; i < 32; i++ ){
    R_0_31 v = inst_in_tick( i );
    
    printf( "%i <> %i\n", i % 8, v );
    if( i%8 != v ){
      return -1;
    }
  }
  return 0;
}

