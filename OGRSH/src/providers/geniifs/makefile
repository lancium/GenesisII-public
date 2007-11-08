include $(OGRSH_HOME)/macros/makefile.in

OOBJ_DIR		=		$(OBJ_DIR)/providers/geniifs

OBJECTS			=		$(OOBJ_DIR)/GeniiFSProvider.o						\
						$(OOBJ_DIR)/GeniiFSSession.o						\
						$(OOBJ_DIR)/GeniiFSMount.o							\
						$(OOBJ_DIR)/GeniiFSDirectoryStream.o				\
						$(OOBJ_DIR)/GeniiFSDirectoryFunctions.o				\
						$(OOBJ_DIR)/GeniiFSDirectoryDescriptor.o			\
						$(OOBJ_DIR)/GeniiFSACLFunctions.o					\
						$(OOBJ_DIR)/GeniiFSFileFunctions.o					\
						$(OOBJ_DIR)/GeniiFSExecuteFunctions.o				\
						$(OOBJ_DIR)/GeniiFSFileDescriptor.o

LIBRARY			=		$(LIB_DIR)/libGenesisIIOGRSHProvider.so

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
		$(CPP) $(CPPFLAGS) -shared -o $@ $(OBJECTS) $(LFLAGS) -lJComm

##

$(OOBJ_DIR)/GeniiFSProvider.o:	GeniiFSProvider.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSProvider.cpp -o $@

$(OOBJ_DIR)/GeniiFSSession.o:	GeniiFSSession.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSSession.cpp -o $@

$(OOBJ_DIR)/GeniiFSMount.o:	GeniiFSMount.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSMount.cpp -o $@

$(OOBJ_DIR)/GeniiFSDirectoryStream.o:	GeniiFSDirectoryStream.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSDirectoryStream.cpp -o $@

$(OOBJ_DIR)/GeniiFSDirectoryFunctions.o:	GeniiFSDirectoryFunctions.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSDirectoryFunctions.cpp -o $@

$(OOBJ_DIR)/GeniiFSDirectoryDescriptor.o:	GeniiFSDirectoryDescriptor.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSDirectoryDescriptor.cpp -o $@

$(OOBJ_DIR)/GeniiFSACLFunctions.o:	GeniiFSACLFunctions.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSACLFunctions.cpp -o $@

$(OOBJ_DIR)/GeniiFSFileFunctions.o:	GeniiFSFileFunctions.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSFileFunctions.cpp -o $@

$(OOBJ_DIR)/GeniiFSExecuteFunctions.o:	GeniiFSExecuteFunctions.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSExecuteFunctions.cpp -o $@

$(OOBJ_DIR)/GeniiFSFileDescriptor.o:	GeniiFSFileDescriptor.cpp
		$(CPP) $(CPPFLAGS) -c GeniiFSFileDescriptor.cpp -o $@
