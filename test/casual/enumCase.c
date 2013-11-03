#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static int test( enumCase_Weekday day, int value ){
  int got = inst_in(day);
  got = got & 0x3;
  if( value != got ){
    printf( "error by %i: expected %i got %i\n", day, value, got );
    return -1;
  } else {
    return 0;
  }
}

int main(){
  inst__construct();
  
  if( test( enumCase_Monday, 2 ) < 0 ) return -1;
  if( test( enumCase_Tuesday, 0 ) < 0 ) return -1;
  if( test( enumCase_Wednesday, 2 ) < 0 ) return -1;
  if( test( enumCase_Thursday, 1 ) < 0 ) return -1;
  if( test( enumCase_Friday, 1 ) < 0 ) return -1;
  if( test( enumCase_Saturday, 1 ) < 0 ) return -1;
  if( test( enumCase_Sunday, 3 ) < 0 ) return -1;

  inst__destruct();
  
  return 0;
}

