include $(OGRSH_HOME)/macros/makefile.in

OOBJ_DIR		=		$(OBJ_DIR)/providers/localfs

OBJECTS			=		$(OOBJ_DIR)/LocalFSProvider.o						\
						$(OOBJ_DIR)/LocalFSSession.o						\
						$(OOBJ_DIR)/LocalFSDirectoryFunctions.o				\
						$(OOBJ_DIR)/LocalFSFileFunctions.o					\
						$(OOBJ_DIR)/LocalFSExecuteFunctions.o				\
						$(OOBJ_DIR)/LocalFSFileDescriptor.o					\
						$(OOBJ_DIR)/LocalFSACLFunctions.o					\
						$(OOBJ_DIR)/LocalFSDirectoryStream.o				\
						$(OOBJ_DIR)/LocalFSMount.o

LIBRARY			=		$(LIB_DIR)/libLocalOGRSHProvider.so

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

$(OOBJ_DIR)/LocalFSProvider.o:	LocalFSProvider.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSProvider.cpp -o $@

$(OOBJ_DIR)/LocalFSSession.o:	LocalFSSession.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSSession.cpp -o $@

$(OOBJ_DIR)/LocalFSDirectoryFunctions.o:	LocalFSDirectoryFunctions.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSDirectoryFunctions.cpp -o $@

$(OOBJ_DIR)/LocalFSACLFunctions.o:	LocalFSACLFunctions.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSACLFunctions.cpp -o $@

$(OOBJ_DIR)/LocalFSFileFunctions.o:	LocalFSFileFunctions.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSFileFunctions.cpp -o $@

$(OOBJ_DIR)/LocalFSExecuteFunctions.o:	LocalFSExecuteFunctions.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSExecuteFunctions.cpp -o $@

$(OOBJ_DIR)/LocalFSFileDescriptor.o:	LocalFSFileDescriptor.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSFileDescriptor.cpp -o $@

$(OOBJ_DIR)/LocalFSDirectoryStream.o:	LocalFSDirectoryStream.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSDirectoryStream.cpp -o $@

$(OOBJ_DIR)/LocalFSMount.o:	LocalFSMount.cpp
		$(CPP) $(CPPFLAGS) -c LocalFSMount.cpp -o $@
