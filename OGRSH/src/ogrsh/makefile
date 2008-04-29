include $(OGRSH_HOME)/macros/makefile.in

OOBJ_DIR		=		$(OBJ_DIR)/ogrsh

OBJECTS			=		$(OOBJ_DIR)/XMLUtilities.o							\
						$(OOBJ_DIR)/EnvironmentVariables.o					\
						$(OOBJ_DIR)/Version.o								\
						$(OOBJ_DIR)/Configuration.o							\
						$(OOBJ_DIR)/XMLParserErrorHandler.o					\
						$(OOBJ_DIR)/ConfigurationFileParser.o				\
						$(OOBJ_DIR)/Path.o									\
						$(OOBJ_DIR)/OGRSH.o									\
						$(OOBJ_DIR)/DynamicallyLoadedSymbol.o				\
						$(OOBJ_DIR)/Provider.o								\
						$(OOBJ_DIR)/Session.o								\
						$(OOBJ_DIR)/Mount.o									\
						$(OOBJ_DIR)/MountTree.o								\
						$(OOBJ_DIR)/Directory.o								\
						$(OOBJ_DIR)/File.o									\
						$(OOBJ_DIR)/Java.o									\
						$(OOBJ_DIR)/Execute.o								\
						$(OOBJ_DIR)/Threads.o								\
						$(OOBJ_DIR)/ExecuteState.o							\
						$(OOBJ_DIR)/ACL.o									\
						$(OOBJ_DIR)/System.o								\
						$(OOBJ_DIR)/DangerSet.o								\
						$(OOBJ_DIR)/DirectoryStream.o						\
						$(OOBJ_DIR)/DirectoryFunctions.o					\
						$(OOBJ_DIR)/ACLFunctions.o							\
						$(OOBJ_DIR)/VirtualMount.o							\
						$(OOBJ_DIR)/VirtualDirectoryFunctions.o				\
						$(OOBJ_DIR)/VirtualACLFunctions.o					\
						$(OOBJ_DIR)/VirtualFileFunctions.o					\
						$(OOBJ_DIR)/VirtualExecuteFunctions.o				\
						$(OOBJ_DIR)/VirtualFileDescriptor.o					\
						$(OOBJ_DIR)/VirtualDirectoryStream.o				\
						$(OOBJ_DIR)/VirtualProviderUtilities.o				\
						$(OOBJ_DIR)/FileDescriptor.o						\
						$(OOBJ_DIR)/FileDescriptorTable.o					\
						$(OOBJ_DIR)/ProtectedTable.o						\
						$(OOBJ_DIR)/FileFunctions.o							\
						$(OOBJ_DIR)/ExecuteFunctions.o						\
						$(OOBJ_DIR)/Random.o

LIBRARY			=		$(LIB_DIR)/libOGRSH.so

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

$(OOBJ_DIR)/OGRSH.o:	OGRSH.cpp
		$(CPP) $(CPPFLAGS) -c OGRSH.cpp -o $@

$(OOBJ_DIR)/DynamicallyLoadedSymbol.o:	DynamicallyLoadedSymbol.cpp
		$(CPP) $(CPPFLAGS) -c DynamicallyLoadedSymbol.cpp -o $@

$(OOBJ_DIR)/Provider.o:	Provider.cpp
		$(CPP) $(CPPFLAGS) -c Provider.cpp -o $@

$(OOBJ_DIR)/Session.o:	Session.cpp
		$(CPP) $(CPPFLAGS) -c Session.cpp -o $@

$(OOBJ_DIR)/Mount.o:	Mount.cpp
		$(CPP) $(CPPFLAGS) -c Mount.cpp -o $@

$(OOBJ_DIR)/MountTree.o:	MountTree.cpp
		$(CPP) $(CPPFLAGS) -c MountTree.cpp -o $@

$(OOBJ_DIR)/XMLUtilities.o:	XMLUtilities.cpp
		$(CPP) $(CPPFLAGS) -c XMLUtilities.cpp -o $@

$(OOBJ_DIR)/Configuration.o:	Configuration.cpp
		$(CPP) $(CPPFLAGS) -c Configuration.cpp -o $@

$(OOBJ_DIR)/EnvironmentVariables.o:	EnvironmentVariables.cpp
		$(CPP) $(CPPFLAGS) -c EnvironmentVariables.cpp -o $@

$(OOBJ_DIR)/Version.o:	Version.cpp
		$(CPP) $(CPPFLAGS) -c Version.cpp -o $@

$(OOBJ_DIR)/XMLParserErrorHandler.o:	XMLParserErrorHandler.cpp
		$(CPP) $(CPPFLAGS) -c XMLParserErrorHandler.cpp -o $@

$(OOBJ_DIR)/ConfigurationFileParser.o:	ConfigurationFileParser.cpp
		$(CPP) $(CPPFLAGS) -c ConfigurationFileParser.cpp -o $@

$(OOBJ_DIR)/Path.o:		Path.cpp
		$(CPP) $(CPPFLAGS) -c Path.cpp -o $@

$(OOBJ_DIR)/Directory.o:		Directory.cpp
		$(CPP) $(CPPFLAGS) -c Directory.cpp -o $@

$(OOBJ_DIR)/File.o:		File.cpp
		$(CPP) $(CPPFLAGS) -c File.cpp -o $@

$(OOBJ_DIR)/Java.o:		Java.cpp
		$(CPP) $(CPPFLAGS) -c Java.cpp -o $@

$(OOBJ_DIR)/Execute.o:		Execute.cpp
		$(CPP) $(CPPFLAGS) -c Execute.cpp -o $@

$(OOBJ_DIR)/Threads.o:		Threads.cpp
		$(CPP) $(CPPFLAGS) -c Threads.cpp -o $@

$(OOBJ_DIR)/ExecuteState.o:		ExecuteState.cpp
		$(CPP) $(CPPFLAGS) -c ExecuteState.cpp -o $@

$(OOBJ_DIR)/ACL.o:		ACL.cpp
		$(CPP) $(CPPFLAGS) -c ACL.cpp -o $@

$(OOBJ_DIR)/System.o:		System.cpp
		$(CPP) $(CPPFLAGS) -c System.cpp -o $@

$(OOBJ_DIR)/DangerSet.o:		DangerSet.cpp
		$(CPP) $(CPPFLAGS) -c DangerSet.cpp -o $@

$(OOBJ_DIR)/DirectoryStream.o:		DirectoryStream.cpp
		$(CPP) $(CPPFLAGS) -c DirectoryStream.cpp -o $@

$(OOBJ_DIR)/DirectoryFunctions.o:		DirectoryFunctions.cpp
		$(CPP) $(CPPFLAGS) -c DirectoryFunctions.cpp -o $@

$(OOBJ_DIR)/ACLFunctions.o:		ACLFunctions.cpp
		$(CPP) $(CPPFLAGS) -c ACLFunctions.cpp -o $@

$(OOBJ_DIR)/VirtualDirectoryFunctions.o:		VirtualDirectoryFunctions.cpp
		$(CPP) $(CPPFLAGS) -c VirtualDirectoryFunctions.cpp -o $@

$(OOBJ_DIR)/VirtualACLFunctions.o:		VirtualACLFunctions.cpp
		$(CPP) $(CPPFLAGS) -c VirtualACLFunctions.cpp -o $@

$(OOBJ_DIR)/VirtualFileFunctions.o:		VirtualFileFunctions.cpp
		$(CPP) $(CPPFLAGS) -c VirtualFileFunctions.cpp -o $@

$(OOBJ_DIR)/VirtualExecuteFunctions.o:		VirtualExecuteFunctions.cpp
		$(CPP) $(CPPFLAGS) -c VirtualExecuteFunctions.cpp -o $@

$(OOBJ_DIR)/VirtualFileDescriptor.o:		VirtualFileDescriptor.cpp
		$(CPP) $(CPPFLAGS) -c VirtualFileDescriptor.cpp -o $@

$(OOBJ_DIR)/VirtualMount.o:		VirtualMount.cpp
		$(CPP) $(CPPFLAGS) -c VirtualMount.cpp -o $@

$(OOBJ_DIR)/VirtualDirectoryStream.o:		VirtualDirectoryStream.cpp
		$(CPP) $(CPPFLAGS) -c VirtualDirectoryStream.cpp -o $@

$(OOBJ_DIR)/VirtualProviderUtilities.o:		VirtualProviderUtilities.cpp
		$(CPP) $(CPPFLAGS) -c VirtualProviderUtilities.cpp -o $@

$(OOBJ_DIR)/FileDescriptor.o:		FileDescriptor.cpp
		$(CPP) $(CPPFLAGS) -c FileDescriptor.cpp -o $@

$(OOBJ_DIR)/FileDescriptorTable.o:		FileDescriptorTable.cpp
		$(CPP) $(CPPFLAGS) -c FileDescriptorTable.cpp -o $@

$(OOBJ_DIR)/ProtectedTable.o:		ProtectedTable.cpp
		$(CPP) $(CPPFLAGS) -c ProtectedTable.cpp -o $@

$(OOBJ_DIR)/FileFunctions.o:		FileFunctions.cpp
		$(CPP) $(CPPFLAGS) -c FileFunctions.cpp -o $@

$(OOBJ_DIR)/ExecuteFunctions.o:		ExecuteFunctions.cpp
		$(CPP) $(CPPFLAGS) -c ExecuteFunctions.cpp -o $@

$(OOBJ_DIR)/Random.o:		Random.cpp
		$(CPP) $(CPPFLAGS) -c Random.cpp -o $@
