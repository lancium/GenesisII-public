all: readtest writetest
read: readtest
write: writetest

readtest:
	mpicc readtest.c -o readtest

writetest:
	mpicc writetest.c -o writetest

clean:
	rm -r readtest writetest
