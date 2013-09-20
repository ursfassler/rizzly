#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

int main(){
  inst__system_construct();
  
  int i;
  for( i = 0; i < 100; i++ ){
    R_0_100 val = inst_in_read();
    R_0_100 exp = i%3==1 ? 42 : 0;
    if( val != exp ){
      printf( "%i:%i\n", i, val );
      return -1;
    }

    inst_in_tick();
  }
  
  inst__system_destruct();

  return 0;
}

