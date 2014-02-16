#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  rec4_Point p1, p2;
  
  p1.x = 4;
  p1.y = 2;
  
  int i;
  for( i = 0; i < 100; i++ ){
    p2.x = i / 10;
    p2.y = i % 10;
    if( inst_equal( p1, p2 ) != ((p1.x == p2.x) && (p1.y == p2.y)) ){
      return -1;
    }
  }
  
  inst__destruct();
  return 0;
}

