include $(PWRAPNATIVE)/macros/makefile.in

OBJECTS		=		$(OBJDIR)/Memory.o										\
					$(OBJDIR)/StringFunctions.o								\
					$(OBJDIR)/LinkedList.o									\
					$(OBJDIR)/HashMap.o										\
					$(OBJDIR)/Fuse.o										\
					$(OBJDIR)/CommandLine.o									\
					$(OBJDIR)/ExitResults.o									\
					$(OBJDIR)/OSSpecific.o

BINARIES	=		$(BINDIR)/pwrapper

###############

all:	$(BINDIR) $(OBJDIR) $(OBJECTS) $(BINARIES)

clean:
	/bin/rm -f -r $(BINDIR) $(OBJDIR)

###############

$(BINDIR):
	mkdir -p $(BINDIR)

$(OBJDIR):
	mkdir -p $(OBJDIR)

###############

$(OBJDIR)/Memory.o:	Memory.c Memory.h
	$(CC) $(CFLAGS) Memory.c -c -o $@

$(OBJDIR)/StringFunctions.o:	StringFunctions.c StringFunctions.h
	$(CC) $(CFLAGS) StringFunctions.c -c -o $@

$(OBJDIR)/LinkedList.o:	LinkedList.c LinkedList.h
	$(CC) $(CFLAGS) LinkedList.c -c -o $@

$(OBJDIR)/HashMap.o:	HashMap.c HashMap.h
	$(CC) $(CFLAGS) HashMap.c -c -o $@

$(OBJDIR)/Fuse.o:	Fuse.c Fuse.h
	$(CC) $(CFLAGS) Fuse.c -c -o $@

$(OBJDIR)/CommandLine.o:	CommandLine.c CommandLine.h
	$(CC) $(CFLAGS) CommandLine.c -c -o $@

$(OBJDIR)/ExitResults.o:	ExitResults.c ExitResults.h
	$(CC) $(CFLAGS) ExitResults.c -c -o $@

$(OBJDIR)/OSSpecific.o:	OSSpecific-unix.c OSSpecific.h
	$(CC) $(CFLAGS) OSSpecific-unix.c -c -o $@

$(BINDIR)/pwrapper:	$(OBJECTS) pwrapper.c
	$(CC) $(CFLAGS) pwrapper.c -o $@ $(OBJECTS) $(LFLAGS)
