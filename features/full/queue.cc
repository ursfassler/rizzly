
#include "queue.h"

#include <queue>
#include <string.h>

static std::queue<std::string> queue;

extern "C"
{

int canRead()
{
  return !queue.empty();
}

int msgSize()
{
  return queue.front().size();
}

void next(char *msg, int size)
{
  strncpy(msg, queue.front().c_str(), size);
  queue.pop();
}

}

void push(const std::string &msg)
{
  queue.push(msg);
}

