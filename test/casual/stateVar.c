#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static uint8_t value;
static uint8_t error = 0;

void inst_out_tick( uint8_t x ){
  printf( "%i (%i)\n", x, value );
  if( value != x ){
    error = 1;
  }
}

int main(){
  inst__system_construct();

  int i;
  for( i = 0; i < 100; i++ ){
    value = (i + 1) % 11;
    inst_in_tick( 0 );
  }
  return -error;
}

