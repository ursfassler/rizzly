from ctypes import *

class Queue:
    def __init__(self):
        self._inst = CDLL('./libinst.so')

    def _next(self):
        size = self._inst.msgSize()
        p = create_string_buffer(size)
        self._inst.next(p, sizeof(p))
        return p.value

    def _canRead(self):
        return int(self._inst.canRead()) != 0


