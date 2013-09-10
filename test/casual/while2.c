#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static R_0_100 next;

void inst_out_foo(R_0_100 x){
  printf( "%i\n", x );
  if( x != next ){
    exit(-1);
  }
  next--;
}

int main(){
  R_0_100 i;
  for( i = 0; i <= 100; i++ ){
    printf( "--> %i\n", i );
    next = i;
    inst_in_foo( i );
    if( next != 0 ){
      return -1;
    }
  }
  return 0;
}

