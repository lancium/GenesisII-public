include $(OGRSH_HOME)/macros/makefile.in

OOBJ_DIR		=		$(OBJ_DIR)/jcomm

OBJECTS			=		$(OOBJ_DIR)/IOException.o						\
						$(OOBJ_DIR)/IOGRSHWriteBuffer.o					\
						$(OOBJ_DIR)/IOGRSHReadBuffer.o					\
						$(OOBJ_DIR)/DefaultOGRSHWriteBuffer.o			\
						$(OOBJ_DIR)/DefaultOGRSHReadBuffer.o			\
						$(OOBJ_DIR)/IPackable.o							\
						$(OOBJ_DIR)/OGRSHException.o					\
						$(OOBJ_DIR)/ByteBuffer.o						\
						$(OOBJ_DIR)/Socket.o							\
						$(OOBJ_DIR)/Invocation.o						\
						$(OOBJ_DIR)/TestingClient.o						\
						$(OOBJ_DIR)/SessionClient.o						\
						$(OOBJ_DIR)/DirectoryClient.o					\
						$(OOBJ_DIR)/FileClient.o						\
						$(OOBJ_DIR)/StatBuffer.o						\
						$(OOBJ_DIR)/DirectoryEntry.o

LIBRARY			=		$(LIB_DIR)/libJComm.so

##########

all:	$(OOBJ_DIR) $(LIBRARY)

clean:
		/bin/rm -f -r $(OBJECTS)
		/bin/rm -f -r $(LIBRARY)
		/bin/rm -f -r $(OOBJ_DIR)

$(OOBJ_DIR):
		mkdir -p $(OOBJ_DIR)

##########################

$(LIBRARY):		$(OBJECTS)
		$(CPP) $(CPPFLAGS) -shared -o $@ $(OBJECTS) $(LFLAGS)

##

$(OOBJ_DIR)/IOException.o:	IOException.cpp
		$(CPP) $(CPPFLAGS) -c IOException.cpp -o $@

$(OOBJ_DIR)/IOGRSHWriteBuffer.o:	IOGRSHWriteBuffer.cpp
		$(CPP) $(CPPFLAGS) -c IOGRSHWriteBuffer.cpp -o $@

$(OOBJ_DIR)/DefaultOGRSHWriteBuffer.o:	DefaultOGRSHWriteBuffer.cpp
		$(CPP) $(CPPFLAGS) -c DefaultOGRSHWriteBuffer.cpp -o $@

$(OOBJ_DIR)/IOGRSHReadBuffer.o:	IOGRSHReadBuffer.cpp
		$(CPP) $(CPPFLAGS) -c IOGRSHReadBuffer.cpp -o $@

$(OOBJ_DIR)/DefaultOGRSHReadBuffer.o:	DefaultOGRSHReadBuffer.cpp
		$(CPP) $(CPPFLAGS) -c DefaultOGRSHReadBuffer.cpp -o $@

$(OOBJ_DIR)/IPackable.o:	IPackable.cpp
		$(CPP) $(CPPFLAGS) -c IPackable.cpp -o $@

$(OOBJ_DIR)/OGRSHException.o:	OGRSHException.cpp
		$(CPP) $(CPPFLAGS) -c OGRSHException.cpp -o $@

$(OOBJ_DIR)/ByteBuffer.o:	ByteBuffer.cpp
		$(CPP) $(CPPFLAGS) -c ByteBuffer.cpp -o $@

$(OOBJ_DIR)/Socket.o:	Socket.cpp
		$(CPP) $(CPPFLAGS) -c Socket.cpp -o $@

$(OOBJ_DIR)/Invocation.o:	Invocation.cpp
		$(CPP) $(CPPFLAGS) -c Invocation.cpp -o $@

$(OOBJ_DIR)/TestingClient.o:	TestingClient.cpp
		$(CPP) $(CPPFLAGS) -c TestingClient.cpp -o $@

$(OOBJ_DIR)/SessionClient.o:	SessionClient.cpp
		$(CPP) $(CPPFLAGS) -c SessionClient.cpp -o $@

$(OOBJ_DIR)/DirectoryClient.o:	DirectoryClient.cpp
		$(CPP) $(CPPFLAGS) -c DirectoryClient.cpp -o $@

$(OOBJ_DIR)/FileClient.o:	FileClient.cpp
		$(CPP) $(CPPFLAGS) -c FileClient.cpp -o $@

$(OOBJ_DIR)/StatBuffer.o:	StatBuffer.cpp
		$(CPP) $(CPPFLAGS) -c StatBuffer.cpp -o $@

$(OOBJ_DIR)/DirectoryEntry.o:	DirectoryEntry.cpp
		$(CPP) $(CPPFLAGS) -c DirectoryEntry.cpp -o $@
