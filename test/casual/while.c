#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

while_Point inst_out_foo(R_0_255 x){
  while_Point ret;
  ret.x = x;
  ret.y = 255 - x;
  return ret;
}

#define check( x1, y1, x2, y2 )    printf( "%i %i <> %i %i\n", x1, y1, x2, y2 ); if( (x1 != x2) | (y1 != y2) ) return -1


int main(){
  inst__entry();

  while_Point point;
  
  point = inst_in_foo( 0 );
  check( point.x, point.y, 0, 0 );
  
  point = inst_in_foo( 1 );
  check( point.x, point.y, 1, 254 );
  
  point = inst_in_foo( 2 );
  check( point.x, point.y, 2, 253 );
  
  point = inst_in_foo( 3 );
  check( point.x, point.y, 3, 252 );
  
  return 0;
}


