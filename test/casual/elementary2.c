#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void inst_tickOut(){
}

int main(){
  inst__construct();

  inst_tickIn();

  inst__destruct();
  
  return 0;
}

