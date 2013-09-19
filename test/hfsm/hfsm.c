#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

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

void inst_evt_evt(R_0_255 value){
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
  inst__system_construct();
  printf( "tick\n" );
  inst_tick_tick();
  printf( "tick\n" );
  inst_tick_tick();
  printf( "tick\n" );
  inst_tick_tick();
  printf( "destruct\n" );
  inst__system_destruct();

  return error;
}

