#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static int test( enumCase_Weekday day, int value ){
  int got = inst_in_tick(day);
  got = got & 0x3;
  if( value != got ){
    printf( "error by %i: expected %i got %i\n", day, value, got );
    return -1;
  } else {
    return 0;
  }
}

int main(){
  inst__system_construct();
  
  if( test( Monday, 2 ) < 0 ) return -1;
  if( test( Tuesday, 0 ) < 0 ) return -1;
  if( test( Wednesday, 2 ) < 0 ) return -1;
  if( test( Thursday, 1 ) < 0 ) return -1;
  if( test( Friday, 1 ) < 0 ) return -1;
  if( test( Saturday, 1 ) < 0 ) return -1;
  if( test( Sunday, 3 ) < 0 ) return -1;

  return 0;
}

