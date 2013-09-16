#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void inst_out_tick(){
  printf("tick\n");
}

int main(){
  inst__entry();
  
  inst_in_tick();
  inst_in_tick();
  inst_in_tick();
  inst_in_tick();
  inst_in_tick();
  inst_in_tick();
  
  return 0;
}

