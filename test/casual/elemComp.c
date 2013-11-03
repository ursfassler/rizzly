#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

static int test( R_0_100 got, R_0_100 exp ){
  got = got & 0x7f;
  exp = exp & 0x7f;
  if( got != exp ){
    printf( "%i <> %i\n", got, exp );
    return -1;
  } else {
    return 0;
  }
}

int main(){
  inst__construct();

  if( test( inst_val1(), 10 ) < 0 ){ return -1; };
  if( test( inst_val2(), 20 ) < 0 ){ return -1; };
  if( test( inst_val3(), 30 ) < 0 ){ return -1; };
  if( test( inst_val4(), 40 ) < 0 ){ return -1; };
  if( test( inst_val5(), 50 ) < 0 ){ return -1; };

  inst__destruct();
  
  if( test( inst_val1(), 20 ) < 0 ){ return -1; };
  if( test( inst_val2(), 30 ) < 0 ){ return -1; };
  if( test( inst_val3(), 40 ) < 0 ){ return -1; };
  if( test( inst_val4(), 50 ) < 0 ){ return -1; };
  if( test( inst_val5(), 60 ) < 0 ){ return -1; };
  
  return 0;
}

