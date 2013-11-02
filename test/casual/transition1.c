#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

static int i;

void inst_out_tick(){
  printf("out\n");
  if( (i % 3) != 2 ){
    printf("error\n");
    exit(-1);
  }
}

int main(){
  inst__system_construct();

  for( i = 0; i < 20; i++ ){  
    printf( "in\n" );
    inst_in_tick();
  }
  
  return 0;
}

