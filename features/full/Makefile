

libinst.so: inst.o instCb.o queue.o
	g++ -fPIC -shared $^ -o $@

inst.o: output/inst.c
	gcc -fPIC  -c $^ -o $@

instCb.o: instCb.cc
	gcc -std=c++11 -fPIC  -c $^ -o $@
	
queue.o: queue.cc
	gcc -std=c++11 -fPIC  -c $^ -o $@
	
clean:
	rm -f *.so *.o *.pyc

