#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  R_0_65536 a;
  R_0_4294967296 z;
  for( a = 1; a <= 65536; a = a << 1 ){
    int64_t exp = a;
    exp = exp * exp;
    z = inst_in( a, a );
    printf( "%i: %li <> %li\n", a, exp, z );
    if( exp != z ){
      return -1;
    }
  }
  inst__destruct();
  return 0;
}

