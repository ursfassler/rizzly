#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  if( inst_get() != 18 ){
    return -1;
  }
  
  inst__destruct();
  return 0;
}

