CC			=		gcc
CFLAGS		=		-I. -Wall
LFLAGS		=

OBJECTS		=		fib.o fileReader.o driver.o
BINARIES	=		FibProgram

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

FibProgram:	$(LIBRARIES) driver.o fib.o fileReader.o
	$(CC) $(CFLAGS) driver.o fib.o fileReader.o $(LFLAGS) -o FibProgram
