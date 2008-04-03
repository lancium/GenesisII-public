CC			=			gcc
CFLAGS		=			-Wall -g
LFLAGS		=

OBJECTS		=			StringUtils.o StringPair.o Test.o PairList.o		\
						JobDescription.o
BINARIES	=			Test

#################

all:	$(OBJECTS) $(BINARIES)

clean:
	/bin/rm -f $(OBJECTS) $(BINARIES)

#################

JobDescription.o:		JobDescription.c JobDescription.h					\
						StringUtils.h StringPair.h PairList.o
	$(CC) $(CFLAGS) JobDescription.c -c -o JobDescription.o

StringUtils.o:			StringUtils.h StringUtils.c
	$(CC) $(CFLAGS) StringUtils.c -c -o StringUtils.o

StringPair.o:			StringPair.h StringPair.c StringUtils.h
	$(CC) $(CFLAGS) StringPair.c -c -o StringPair.o

PairList.o:			StringPair.h StringUtils.h PairList.h PairList.c
	$(CC) $(CFLAGS) PairList.c -c -o PairList.o

Test.o:		StringPair.h StringUtils.h Test.c
	$(CC) $(CFLAGS) Test.c -c -o Test.o

Test:			$(OBJECTS)
	$(CC) $(CFLAGS) $(OBJECTS) -o Test $(LFLAGS)
