#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

static R_0_100 out;

void inst_out(R_0_100 x){
  out = x;
}

int main(){
  inst__construct();

  for( int i = 0; i < 10; i++ ){
    inst_ind( i );
  }
  
  printf( "%i\n", inst__queue_count() );
  for( int i = 0; i < 10; i++ ){
    out = 100;
    inst__queue_dispatch();
    if( out != i ){
      return -1;
    }
  }
  
  if(inst__queue_count() != 0 ) {
    return -1;
  }
  
  inst__destruct();

  return 0;
}

