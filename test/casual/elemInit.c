#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  int i;
  for( i = 0; i < 100; i++ ){
    if( inst_in_get() != 42 ){
      return -1;
    }
  }
  return 0;
}

