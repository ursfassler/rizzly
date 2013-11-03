#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  int i;
  R_0_199 a[20];

  for( i = 0; i < 20; i++ ){
    a[i] = i * 3;
    inst_set( i, i * 3 );
  }

  for( i = 0; i < 20; i++ ){
    R_0_199 v = inst_get( i );
    
    printf( "%i: %i <> %i\n", i, a[i], v );
    if( a[i] != v ){
      return -1;
    }
  }
  
  inst__destruct();
  
  return 0;
}

