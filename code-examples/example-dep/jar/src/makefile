CC			=		gcc
CFLAGS		=		-I. -Wall
LFLAGS		=

OBJECTS		=		fib.o fileReader.o driver.o
BINARIES	=		FibProgram
JAR_FILE	=		FibProgram.jar

###################################################3

all:	$(JAR_FILE)

clean:
		/bin/rm -f -r $(BINARIES) $(OBJECTS) $(JAR_FILE)

###################################################

fib.o:	fib.c fib.h fileReader.h
	$(CC) $(CFLAGS) fib.c -c -o fib.o

fileReader.o:	fileReader.c fileReader.h
	$(CC) $(CFLAGS) fileReader.c -c -o fileReader.o

driver.o:	driver.c fib.h fileReader.h
	$(CC) $(CFLAGS) driver.c -c -o driver.o

FibProgram:	$(OBJECTS)
	$(CC) $(CFLAGS) $(OBJECTS) $(LFLAGS) -o FibProgram

FibProgram.jar:	$(BINARIES)
	jar -cvf FibProgram.jar FibProgram one.dat zero.dat
