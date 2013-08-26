#!/bin/bash

##Author: Vanamala Venkataswamy

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

which make
if [ $? == 0 ]
then
	make clean
	make all
else
	which gmake
	if [ $? == 0 ]
	then
        	gmake clean
        	gmake all
	else
		echo "make/gmake not available, cannot proceed. Exiting..."
		exit
	fi
fi

which tar
if [ $? == 0 ]
then
	cd ../
	tar cvf ./tar-file.tar GFFS*
	grid cp local:./tar-file.tar $RNSPATH
	cd -
else
	echo "tar not found, cannot proceed. Exiting..."
fi

which qsub
if [ $? == 0 ]
then
	qsub -v "GENII_INSTALL_DIR=$GENII_INSTALL_DIR","RNSPATH=$RNSPATH" readtest.sh
	qsub -v "GENII_INSTALL_DIR=$GENII_INSTALL_DIR","RNSPATH=$RNSPATH" writetest.sh
else
	echo "qsub not available, cannot proceed. Exiting..."
fi

wait_for_all_pending_jobs


