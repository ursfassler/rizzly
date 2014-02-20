#include  <stdint.h>
#include  "output/inst.h"

int main(){
  inst__construct();

  if( inst_invert( true ) != false ){
    return -1;
  }  
  if( inst_invert( false ) != true ){
    return -1;
  }  

  inst__destruct();

  return 0;
}

