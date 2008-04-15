CC			=			gcc
CFLAGS		=			-Wall -g -static
LFLAGS		=			-static

OBJECTS		=			StringUtils.o StringPair.o JSDLFormer.o PairList.o	\
						JobDescription.o JSDLWriter.o POSIXAppWriter.o		\
						HPCAppWriter.o
BINARIES	=			JSDLFormer

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

JSDLFormer.o:		StringPair.h StringUtils.h JSDLFormer.c PairList.h		\
		JobDescription.h JSDLWriter.h POSIXAppWriter.h HPCAppWriter.h
	$(CC) $(CFLAGS) JSDLFormer.c -c -o JSDLFormer.o

POSIXAppWriter.o:		StringPair.h StringUtils.h POSIXAppWriter.c			\
		PairList.h JobDescription.h
	$(CC) $(CFLAGS) POSIXAppWriter.c -c -o POSIXAppWriter.o

HPCAppWriter.o:		StringPair.h StringUtils.h HPCAppWriter.c				\
		PairList.h JobDescription.h
	$(CC) $(CFLAGS) HPCAppWriter.c -c -o HPCAppWriter.o

JSDLWriter.o:		StringPair.h StringUtils.h JSDLWriter.c					\
		PairList.h JobDescription.h HPCAppWriter.h POSIXAppWriter.h
	$(CC) $(CFLAGS) JSDLWriter.c -c -o JSDLWriter.o

JSDLFormer:			$(OBJECTS)
	$(CC) $(CFLAGS) $(OBJECTS) -o JSDLFormer $(LFLAGS)
