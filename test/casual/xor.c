#include  <stdint.h>
#include  <stdio.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  for(uint16_t a = 0; a <= 255; a++){
    for(uint16_t b = 0; b <= 255; b++){
      R_0_255 val = inst_calc( a, b );
      R_0_255 exp = a ^ b;
      if( exp != val ){
        printf( "%i <> %i\n", exp, val );
        return -1;
      }
    }
  }
    
  inst__destruct();

  return 0;
}

