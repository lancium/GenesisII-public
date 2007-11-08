include $(OGRSH_HOME)/macros/makefile.in

OOBJ_DIR		=		$(OBJ_DIR)/tests

OBJECTS			=		$(OOBJ_DIR)/TestPath.o								\
						$(OOBJ_DIR)/TestJComm.o								\
						$(OOBJ_DIR)/TestSizes.o

BINARIES		=		$(BIN_DIR)/TestPath									\
						$(BIN_DIR)/TestJComm								\
						$(BIN_DIR)/TestSizes

##########

all:	$(OOBJ_DIR) $(BINARIES)

clean:
		/bin/rm -f -r $(OBJECTS)
		/bin/rm -f -r $(OOBJ_DIR)
		/bin/rm -f -r $(BINARIES)

$(OOBJ_DIR):
		mkdir -p $(OOBJ_DIR)

##########################

$(OOBJ_DIR)/TestPath.o:		TestPath.cpp
		$(CPP) $(CPPFLAGS) -c TestPath.cpp -o $@

$(OOBJ_DIR)/TestJComm.o:		TestJComm.cpp
		$(CPP) $(CPPFLAGS) -c TestJComm.cpp -o $@

$(OOBJ_DIR)/TestSizes.o:		TestSizes.c
		$(CC) -c TestSizes.c -o $@

$(BIN_DIR)/TestPath:	$(OOBJ_DIR)/TestPath.o
		$(CPP) $(CPPFLAGS) $(OOBJ_DIR)/TestPath.o -o $@ $(LFLAGS) -lOGRSH

$(BIN_DIR)/TestJComm:	$(OOBJ_DIR)/TestJComm.o
		$(CPP) $(CPPFLAGS) $(OOBJ_DIR)/TestJComm.o -o $@ $(LFLAGS) -lOGRSH -lJComm

$(BIN_DIR)/TestSizes:	$(OOBJ_DIR)/TestSizes.o
		$(CC) $(OOBJ_DIR)/TestSizes.o -o $@
