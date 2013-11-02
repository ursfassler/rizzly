#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

int main(){
  inst__system_construct();
  
  int i;
  for( i = 0; i < 100; i++ ){
    R_0_100 val = inst_in_read();
    if( val != 57 ){
      printf( "%i:%i\n", i, val );
      return -1;
    }

    inst_in_tick();
  }
  
  inst__system_destruct();

  return 0;
}

