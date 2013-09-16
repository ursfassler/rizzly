#include  "output/inst.h"

void inst_out1_in(R_0_10 x){
}

R_0_10 inst_out1_out(){
  return 10;
}

void inst_out2_in(R_0_2000 x){
}

R_0_2000 inst_out2_out(){
  return 2000;
}

int main( int argc, char **argv ){
  inst__system_construct();

  R_0_2000 n2000;
  R_0_10   n10;
  
  n10 = inst_in1_out();
  inst_in1_in( n10 );
  
  n2000 = inst_in2_out();
  inst_in2_in( n2000 );
  
  return 0;
}

