#include  <stdio.h>
#include  <stdint.h>
#include  "output/inst.h"

void printMsg( uint8_t *list, int size ){
  int i;
  for( i = size-1; i >= 0; i-- ){
    printf( DEBUG_NAMES[list[i]] );
    if( i > 0 ){
      printf( "." );
    }
  }
}

void inst__debug_msgSend(Pointer_Array_3_R_0_3 sender, R_0_2 size){
  printf( "send: " );
  printMsg( *sender, size );
  printf( "\n" );
}

void inst__debug_msgRecv(Pointer_Array_3_R_0_3 receiver, R_0_2 size){
  printf( "recv: " );
  printMsg( *receiver, size );
  printf( "\n" );
}


void inst_out_foo(){
  printf( "out-foo\n" );
}

void inst_out_bar(){
  printf( "out-bar\n" );
}

int main(){
  inst__entry();

  printf( "in-foo\n" );
  inst_in_foo();

  printf( "in-bar\n" );
  inst_in_bar();
  
 return 0;
}

