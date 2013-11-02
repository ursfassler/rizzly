#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

void printMsg( uint8_t *list, int size ){
  int i;
  for( i = size-1; i >= 0; i-- ){
    printf( DEBUG_NAMES[list[i]] );
    if( i > 0 ){
      printf( "." );
    }
  }
}

void inst__debug_msgSend(Array_3_R_0_6 sender, R_0_2 size){
  printf( "send: " );
  printMsg( sender, size );
  printf( "\n" );
}

void inst__debug_msgRecv(Array_3_R_0_6 receiver, R_0_2 size){
  printf( "recv: " );
  printMsg( receiver, size );
  printf( "\n" );
}


void inst_out_foo(){
  printf( "out-foo\n" );
}

void inst_out_bar(){
  printf( "out-bar\n" );
}

int main(){
  inst__system_construct();

  printf( "in-foo\n" );
  inst_in_foo();

  printf( "in-bar\n" );
  inst_in_bar();
  
 return -1;   //TODO implement correct test (look at composition.c)
}

