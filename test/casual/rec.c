#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

static uint8_t value;
static uint8_t error = 0;

void inst_out_tick( uint8_t x ){
  printf( "%i (%i)\n", x, value );
  if( value != x ){
    error = 1;
  }
}

int main(){
  int i;
  for( i = 0; i < 100; i++ ){
    value = (200 - i) / 2;
    inst_in_tick( i );
  }
  return -error;
}

/*
0 -> 100
1 -> 99
2 -> 99
3 -> 98
4 -> 98
5 -> 97
6 -> 97
7 -> 96
8 -> 96
9 -> 95
10 -> 95
11 -> 94
12 -> 94
13 -> 93
*/

// y := 100 - (y + x) mod 100

