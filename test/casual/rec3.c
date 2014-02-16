#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  rec3_Point p;
  int i;
  for( i = 0; i < 100; i++ ){
    p.x = i;
    p.y = 100-i;
    if( inst_getElem( p, 0 ) != i ){
      return -1;
    }
    if( inst_getElem( p, 1 ) != 100-i ){
      return -1;
    }
  }
  
  inst__destruct();
  return 0;
}

