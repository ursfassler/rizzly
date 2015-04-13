#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int exp = 0;

void inst_out(R_0_255 x){
  if(x != exp) {
    printf("expected %i, got%i\n", x, exp);
    exit(-1);
  }
  
  exp++;
}

int main(){
  inst__construct();
  inst_ind();
  inst__destruct();
  
  return 0;
}

