#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

static uint8_t value;
static uint8_t error = 0;

void inst_out( uint8_t x ){
  printf( "%i (%i)\n", x, value );
  if( value != x ){
    error = 1;
  }
}

int main(){
  inst__construct();

  int i;
  for( i = 0; i < 100; i++ ){
    value = 100 - (i % 101);
    inst_in( i % 101 );
  }
  
  inst__destruct();
  return -error;
}

