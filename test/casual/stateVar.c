#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

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
    value = (i + 1) % 11;
    inst_in( 0 );
  }
  
  inst__destruct();
  
  return -error;
}

