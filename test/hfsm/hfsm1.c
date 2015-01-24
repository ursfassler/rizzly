#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

#define NUM_STEP 27

static const int values[NUM_STEP] = {
  10, 20, 30,
  39, 29, 19, 1, 50, 60, 70,
  79, 69, 59, 3, 10, 20, 30,
  39, 29, 19, 1, 50, 60, 70,
  79, 69, 59
};

static int step = 0;
static int error = 0;

void inst_evt(R_1_79 value){
  if( step >= NUM_STEP ){
    printf( "Too many steps" );
    goto error;
  }
  
  printf( "%i", value );
  
  const int exp = values[step];
  step++;
  
  if( exp != value ){
    printf( " expected %i", exp );
    goto error;
  }

  printf( "\n" );
  return;
    
  error:
  error = -1;
  
  printf( "\n" );
}

int main(){
  printf( "construct\n" );
  inst__construct();
  printf( "tick\n" );
  inst_tick();
  printf( "tick\n" );
  inst_tick();
  printf( "tick\n" );
  inst_tick();
  printf( "destruct\n" );
  inst__destruct();

  return error;
}

