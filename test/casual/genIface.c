#include  "output/inst.h"

void inst_out1in(R_0_10 x){
}

R_0_10 inst_out1out(){
  return 10;
}

void inst_out2in(R_0_2000 x){
}

R_0_2000 inst_out2out(){
  return 2000;
}

int main( int argc, char **argv ){
  inst__construct();

  R_0_2000 n2000;
  R_0_10   n10;
  
  n10 = inst_in1out();
  inst_in1in( n10 );
  
  n2000 = inst_in2out();
  inst_in2in( n2000 );
  
  inst__destruct();
  return 0;
}

