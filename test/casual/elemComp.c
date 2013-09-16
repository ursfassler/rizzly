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
  inst__system_construct();

  if( test( inst_val1_get(), 10 ) < 0 ){ return -1; };
  if( test( inst_val2_get(), 20 ) < 0 ){ return -1; };
  if( test( inst_val3_get(), 30 ) < 0 ){ return -1; };
  if( test( inst_val4_get(), 40 ) < 0 ){ return -1; };
  if( test( inst_val5_get(), 50 ) < 0 ){ return -1; };

  inst__system_destruct();
  
  if( test( inst_val1_get(), 20 ) < 0 ){ return -1; };
  if( test( inst_val2_get(), 30 ) < 0 ){ return -1; };
  if( test( inst_val3_get(), 40 ) < 0 ){ return -1; };
  if( test( inst_val4_get(), 50 ) < 0 ){ return -1; };
  if( test( inst_val5_get(), 60 ) < 0 ){ return -1; };
  
  return 0;
}

