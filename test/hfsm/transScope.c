#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

#define NUM_STEP 18

static const char values[NUM_STEP] = {
  'A', '0', '0', '0', '0',
  'B', '0', '0',
  'A', '0', '0', '0', '0',
  'B', '0', '0',
  'A', '0'
};

static char value = 0;

void  inst_outA(){
  value = 'A';
}

void  inst_outB(){
  value = 'B';
}

#define test( func, idx )  value = '0'; func(); printf( "%2i %s %c %c\n", idx, #func, values[idx], value ); if( values[idx] != value ){ exit(-1); }

int main(){
  test( inst__construct, 0 );
  
  int i;
  for( i = 1; i < NUM_STEP-1; i++ ){
    test( inst_in, i );
  }

  test( inst__destruct, NUM_STEP-1 );

  return 0;
}

