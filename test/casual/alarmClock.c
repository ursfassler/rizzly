#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static int i;

void inst_alarm_tick(){
  printf( "alarm by %i\n", i );
  if( i != 5 ){
    printf( "error, expected by 5\n" );
    exit( -1 );
  }
}

int main(){
  inst__system_construct();

  for( i = 0; i < 15; i++ ){
    printf( "tick %i\n", i );
    inst_tick_tick();
  }
  return 0;
}

