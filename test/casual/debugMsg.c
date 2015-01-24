#include  <stdio.h>
#include  <stdint.h>
#include  <stdlib.h>
#include  <string.h>
#include  "output/inst.h"

void _trap(){
  exit( EXIT_FAILURE );
}

#define NUM_STEP 10

typedef enum {
  DBG_RECV,
  DBG_SEND,
  RECV_FOO,
  RECV_BAR
} What;

typedef struct {
  What  what;
  int   size;
  char* path[2];
} Step;

static const Step events[NUM_STEP] = {
  { DBG_RECV, 1, {"_construct"} },

  { DBG_RECV, 1, {"inFoo"} },
  { DBG_SEND, 1, {"outFoo"} },
  { RECV_FOO, 0, {} },
  { DBG_SEND, 1, {"outBar" } },
  { RECV_BAR, 0, {} },
  { DBG_RECV, 1, {"inBar"} },
  { DBG_SEND, 1, {"outBar"} },
  { RECV_BAR, 0, {} },

  { DBG_RECV, 1, {"_destruct"} },
};

static int step = 0;
static int error = 0;

static void printMsg( uint8_t *list, int size ){
  int i;
  for( i = size-1; i >= 0; i-- ){
    printf( inst__DebugName(list[i]) );
    if( i > 0 ){
      printf( "." );
    }
  }
}

static void printError( const Step *as ){
  printf( " => error; expected: " );
  int i;
  for( i = 0; i < as->size; i++ ){
    printf( as->path[i] );
    if( i < as->size-1 ){
      printf( "." );
    }
  }
}

static void checkStep( What what, uint8_t *list, int size ){
  if( step >= NUM_STEP ){
    printf( "Too many steps" );
    goto error;
  }
  
  switch( what ){
    case DBG_RECV: printf( "recv" ); break;
    case DBG_SEND: printf( "send" ); break;
    case RECV_FOO: printf( "foo" ); break;
    case RECV_BAR: printf( "bar" ); break;
  }
  printf( ": " );
  printMsg( list, size );
  
  const Step *as = &events[step];
  step++;
  
  if( as->size != size ){
    printError( as );
    goto error;
  }
  int i;
  for( i = 0; i < size; i++ ){
    if( strcmp( as->path[i], inst__DebugName(list[size-1-i]) ) != 0 ){
      printError( as );
      goto error;
    }
  }

  printf( "\n" );
  return;
    
  error:
  error = -1;
  
  printf( "\n" );
}

void inst__msgSend(Array_3_R_0_6 sender, R_0_2 size){
  checkStep( DBG_SEND, sender.data, size );
}

void inst__msgRecv(Array_3_R_0_6 receiver, R_0_2 size){
  checkStep( DBG_RECV, receiver.data, size );
}

void inst_outFoo(){
  checkStep( RECV_FOO, NULL, 0 );
}

void inst_outBar(){
  checkStep( RECV_BAR, NULL, 0 );
}

int main(){
  inst__construct();
  inst_inFoo();
  inst_inBar();
  inst__destruct();
  
  return error;
}

