#include  <stdio.h>
#include  <stdint.h>
#include  <string.h>
#include  "output/inst.h"

#define NUM_STEP 23

typedef enum {
  DBG_RECV,
  DBG_SEND,
  RECV
} What;

typedef struct {
  What  what;
  int   size;
  char* path[2];
} Step;

static const Step events[NUM_STEP] = {
  { DBG_RECV, 1, {"_construct",  NULL  } },
  { DBG_RECV, 2, {"a", "_construct" } },
  { DBG_RECV, 2, {"b", "_construct" } },
  { DBG_RECV, 2, {"c", "_construct" } },

  { DBG_RECV, 1, {"in", NULL  } },
  { DBG_RECV, 2, {"a", "in" } },
  { DBG_SEND, 2, {"a", "out" } },
  { DBG_RECV, 2, {"b", "in" } },
  { DBG_SEND, 2, {"b", "out" } },
  { DBG_SEND, 1, {"out", NULL } },
  { RECV,     0, {NULL, NULL } },
  { DBG_RECV, 2, {"c", "in" } },
  { DBG_SEND, 2, {"c", "out" } },
  { DBG_SEND, 1, {"out", NULL } },
  { RECV,     0, {NULL, NULL } },
  { DBG_RECV, 2, {"b", "in" } },
  { DBG_SEND, 2, {"b", "out" } },
  { DBG_SEND, 1, {"out", NULL } },
  { RECV,     0, {NULL, NULL } },

  { DBG_RECV, 1, {"_destruct",   NULL  } },
  { DBG_RECV, 2, {"c",        "_destruct" } },
  { DBG_RECV, 2, {"b",        "_destruct" } },
  { DBG_RECV, 2, {"a" ,       "_destruct" } }
};

static int step = 0;
static int error = 0;

static void printMsg( uint8_t *list, int size ){
  int i;
  for( i = size-1; i >= 0; i-- ){
    printf( DEBUG_NAMES[list[i]] );
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
    case RECV:     printf( "outp" ); break;
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
    if( strcmp( as->path[i], DEBUG_NAMES[list[size-1-i]] ) != 0 ){
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

void inst__msgSend(Array_4_R_0_6 sender, R_0_3 size){
  checkStep( DBG_SEND, sender, size );
}

void inst__msgRecv(Array_4_R_0_6 receiver, R_0_3 size){
  checkStep( DBG_RECV, receiver, size );
}

void inst_out(){
  checkStep( RECV, NULL, 0 );
}

int main(){
  inst__construct();

  printf( "send message :in\n" );
  inst_in();
  
  inst__destruct();

  return error;
}

