#!/bin/bash

##Author: Vanamala Venkataswamy

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

which tar
if [ $? == 0 ]
then
	tar xvf iozone3_397.tar
else
	echo "tar not found, cannot proceed. Exiting"
fi

which make
if [ $? == 0 ]
then
	cd iozone3_397/src/current/
	touch *.c
        make $1
else
        which gmake
        if [ $? == 0 ]
        then
		cd iozone3_397/src/current/
		gmake $1
        else
                echo "make/gmake not available, cannot proceed. Exiting..."
                exit
        fi
fi

cd -

if [ -e ./iozone ]
then
	rm ./iozone
fi

cp iozone3_397/src/current/iozone .

if [ -e /Grid_Mount_Point ]
then
	rmdir ./Grid_Mount_Point
fi

mkdir ./Grid_Mount_Point
nohup $GENII_INSTALL_DIR/grid fuse --mount local:./Grid_Mount_Point &>$TEST_TEMP/fusemount.performance
sleep 20
checkMount=`mount` 
if [[ "$checkMount" =~ .*Grid_Mount_Point.* ]]
then
	echo "Grid mounted"
else
	echo "Mount failed, exiting..."
	exit 0
fi

$GENII_INSTALL_DIR/grid cat > ./Grid_Mount_Point$RNSPATH/iozone.txt
./iozone -a -f ./Grid_Mount_Point$RNSPATH/iozone.txt

$GENII_INSTALL_DIR/grid fuse --unmount local:./Grid_Mount_Point
