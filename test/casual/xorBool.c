#include  <stdint.h>
#include  <stdio.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  for(uint8_t a = 0; a < 100; a++){
    for(uint8_t b = 0; b < 100; b++){
      uint8_t val = inst_calc( a, b );
      uint8_t exp = !!a != !!b;
      if( exp != val ){
        printf( "%i <> %i\n", exp, val );
        return -1;
      }
    }
  }
    
  inst__destruct();

  return 0;
}

