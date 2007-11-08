include $(OGRSH_HOME)/macros/makefile.in

all:
	make -e setup-dirs
	cd localfs; make -e all
	cd geniifs; make -e all

clean:
	cd geniifs; make -e clean
	cd localfs; make -e clean
	make -e clean-dirs

setup-dirs:
	mkdir -p $(OBJ_DIR)/providers

clean-dirs:
	/bin/rm -f -r $(OBJ_DIR)/providers
