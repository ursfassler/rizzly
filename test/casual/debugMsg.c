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

void inst__msgSend(Array_3_R_0_5 sender, R_0_2 size){
  printf( "send: " );
  printMsg( sender, size );
  printf( "\n" );
}

void inst__msgRecv(Array_3_R_0_5 receiver, R_0_2 size){
  printf( "recv: " );
  printMsg( receiver, size );
  printf( "\n" );
}


void inst_outFoo(){
  printf( "out-foo\n" );
}

void inst_outBar(){
  printf( "out-bar\n" );
}

int main(){
  inst__construct();

  printf( "in-foo\n" );
  inst_inFoo();

  printf( "in-bar\n" );
  inst_inBar();
  
  inst__destruct();
  
  return -1;   //TODO implement correct test (look at composition.c)
}

