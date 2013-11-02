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
  char* path[3];
} Step;

static const Step events[NUM_STEP] = {
  { DBG_RECV, 2, {"_system",  "construct",  NULL  } },
  { DBG_RECV, 3, {"a",        "_system",    "construct" } },
  { DBG_RECV, 3, {"b",        "_system",    "construct" } },
  { DBG_RECV, 3, {"c" ,       "_system",    "construct" } },

  { DBG_RECV, 2, {"in",   "foo",  NULL  } },
  { DBG_RECV, 3, {"a",    "in",   "foo" } },
  { DBG_SEND, 3, {"a",    "out",  "foo" } },
  { DBG_RECV, 3, {"b" ,   "in",   "foo" } },
  { DBG_SEND, 3, {"b",    "out",  "foo" } },
  { DBG_SEND, 2, {"out",  "foo",  NULL  } },
  { RECV,     0, {NULL,   NULL,   NULL  } },
  { DBG_RECV, 3, {"c",    "in",   "foo" } },
  { DBG_SEND, 3, {"c",    "out",  "foo" } },
  { DBG_SEND, 2, {"out",  "foo",  NULL  } },
  { RECV,     0, {NULL,   NULL,   NULL  } },
  { DBG_RECV, 3, {"b",    "in",   "foo" } },
  { DBG_SEND, 3, {"b",    "out",  "foo" } },
  { DBG_SEND, 2, {"out",  "foo",  NULL  } },
  { RECV,     0, {NULL,   NULL,   NULL  } },

  { DBG_RECV, 2, {"_system",  "destruct",   NULL  } },
  { DBG_RECV, 3, {"c",        "_system",    "destruct" } },
  { DBG_RECV, 3, {"b",        "_system",    "destruct" } },
  { DBG_RECV, 3, {"a" ,       "_system",    "destruct" } }
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

void inst__debug_msgSend(Array_4_R_0_8 sender, R_0_3 size){
  checkStep( DBG_SEND, sender, size );
}

void inst__debug_msgRecv(Array_4_R_0_8 receiver, R_0_3 size){
  checkStep( DBG_RECV, receiver, size );
}

void inst_out_foo(){
  checkStep( RECV, NULL, 0 );
}

int main(){
  inst__system_construct();

  printf( "send message :in.foo\n" );
  inst_in_foo();
  
  inst__system_destruct();

  return error;
}

