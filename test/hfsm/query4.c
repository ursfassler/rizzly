#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

int main(){
  inst__system_construct();
  
  int i;
  for( i = 0; i < 100; i++ ){
    R_0_100 val = inst_in_read( i % 11 );
    R_0_100 exp = (i % 11) * (1 + (i % 3));
    if( val != exp ){
      printf( "%i:%i:%i\n", i, val, exp );
      return -1;
    }

    inst_in_tick();
  }
  
  inst__system_destruct();

  return 0;
}

