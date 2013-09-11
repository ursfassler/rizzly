#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__entry();

  int i;
  R_0_199 a[20];

  for( i = 0; i < 20; i++ ){
    a[i] = i * 3;
    inst_in_set( i, i * 3 );
  }

  for( i = 0; i < 20; i++ ){
    R_0_199 v = inst_in_get( i );
    
    printf( "%i: %i <> %i\n", i, a[i], v );
    if( a[i] != v ){
      return -1;
    }
  }
  
  return 0;
}

