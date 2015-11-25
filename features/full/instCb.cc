#include "queue.h"

#include <string>

extern "C"
{

void _trap()
{
  push("_trap()");
}

void inst_on()
{
  push("on()");
}

void inst_off()
{
  push("off()");
}

void inst_clickCount(int count)
{
  push("clickCount("+std::to_string(count)+")");
}



}


