CC			=		gcc
CFLAGS		=		-I. -Wall
LFLAGS		=		-L. -lFib

OBJECTS		=		fib.o fileReader.o driver.o
BINARIES	=		FibProgram
LIBRARIES	=		libFib.so

###################################################3

all:	$(BINARIES)

clean:
		/bin/rm -f -r $(BINARIES) $(LIBRARIES) $(OBJECTS)

###################################################

fib.o:	fib.c fib.h fileReader.h
	$(CC) $(CFLAGS) fib.c -c -o fib.o

fileReader.o:	fileReader.c fileReader.h
	$(CC) $(CFLAGS) fileReader.c -c -o fileReader.o

driver.o:	driver.c fib.h fileReader.h
	$(CC) $(CFLAGS) driver.c -c -o driver.o

libFib.so:	fib.o fileReader.o
	$(CC) -shared fib.o fileReader.o -o libFib.so

FibProgram:	$(LIBRARIES) driver.o
	$(CC) $(CFLAGS) driver.o $(LFLAGS) -o FibProgram
