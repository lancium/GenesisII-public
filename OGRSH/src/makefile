include $(OGRSH_HOME)/macros/makefile.in

all:
	make -e setup-dirs
	cd ogrsh; make -e all
	cd jcomm; make -e all
	cd providers; make -e all
	cd tests; make -e all
	cd ../java; ant

clean:
	cd ../java; ant clean
	cd tests; make -e clean
	cd providers; make -e clean
	cd jcomm; make -e clean
	cd ogrsh; make -e clean
	make -e clean-dirs

setup-dirs:
	mkdir -p $(OBJ_DIR)
	mkdir -p $(BIN_DIR)
	mkdir -p $(JSERV_DIR)

clean-dirs:
	/bin/rm -f -r $(JSERV_DIR)
	/bin/rm -f -r $(OBJ_DIR)
	/bin/rm -f -r $(BIN_DIR)
