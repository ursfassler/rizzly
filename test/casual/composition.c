#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

void inst_out_foo(){
  printf( "hi\n" );
}

int main(){
  inst_in_foo();
  return 0;
}

