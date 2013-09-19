#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

static int rv = -1;
static int error = 0;

void inst_out_tick(R_0_63 value){
  value = value & 0x3f;
  
  rv = value;
}

int main(){
  inst__system_construct();
  
  printf( "tick,received,expected,error\n" );
  
  int i;
  for( i = 0; i < 64; i++ ){
    rv = -1;
    inst_in_tick( i );
    
    int exp = -1;
    if( i & 0x01 ){
      exp = 0;
    } else if( i & 0x02 ){
      exp = 1;
    } else if( i & 0x04 ){
      exp = 2;
    } else if( i & 0x08 ){
      exp = 3;
    } else if( i & 0x10 ){
      exp = 4;
    } else if( i & 0x20 ){
      exp = 5;
    }
    
    printf( "%i,%i,%i,%i\n", i, rv, exp, rv != exp );
    
    if( rv != exp ){
      error = -1;
    }
  }
 
  inst__system_destruct();

  return error;
}

