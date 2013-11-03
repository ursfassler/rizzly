#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  int i;
  for( i = 0; i < 100; i++ ){
    if( inst_in() != 42 ){
      return -1;
    }
  }
  
  inst__destruct();

  return 0;
}

