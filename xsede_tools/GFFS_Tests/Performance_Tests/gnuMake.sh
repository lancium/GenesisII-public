#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

MOUNT_POINT=$WORKDIR/mount-gnuMake
MAKE_DIR_SUFFIX="${USER}_building"
MAKE_DIR=$MOUNT_POINT/$RNSPATH/$MAKE_DIR_SUFFIX

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # remove any previous mount point.
  echo test mount point is $MOUNT_POINT
  fusermount -u $MOUNT_POINT &>/dev/null  # don't care.
  if [ -e $MOUNT_POINT ]; then
	rmdir $MOUNT_POINT
	if [ $? != 0 ]
	then
		echo "$MOUNT_POINT not empty or something is wrong with that directory. Cannot proceed, bailing out"
		exit 0
	fi
  fi
}

testMountingFuseDirectory()
{
  if ! fuse_supported; then return 0; fi
  # create a new mount point.
  mkdir $MOUNT_POINT
  fuse --mount local:$MOUNT_POINT
  sleep 30  # pause to let it establish the mount.
  checkMount=`mount`
  if [[ "$checkMount" =~ .*$MOUNT_POINT.* ]]
  then
        retval=0
  else
	retval=1
	echo  "Could not mount...Bailing out"
	exit 0
  fi
  grid ls /
  assertEquals "Simple check on root of filesystem on $MOUNT_POINT" 0 $retval
}

testUnTarOnMountedDir()
{
  if ! fuse_supported; then return 0; fi
  mkdir -p $MAKE_DIR
  assertEquals "making compile directory should work" 0 $?
  grid ls $RNSPATH/$MAKE_DIR_SUFFIX
  assertEquals "listing new directory should succeed" 0 $?
  
  cp gzip-1.2.4.tar $MAKE_DIR
  cp iozone3_397.tar $MAKE_DIR
  sync
  pushd "$MAKE_DIR" &>/dev/null
  echo "extracting tar files to '$MAKE_DIR'..."
  # stop taking out the write bits on new files.
  tar xvf $MAKE_DIR/gzip-1.2.4.tar
  assertEquals "Performing 'tar xvf gzip-1.2.4.tar'" 0 $?
  tar xvf $MAKE_DIR/iozone3_397.tar
  assertEquals "Performing 'tar xvf iozone3_397.tar'" 0 $?
  sync
  popd &>/dev/null
}

testMakeOnMountedDir()
{
  if ! fuse_supported; then return 0; fi
  pushd $MAKE_DIR/gzip-1.2.4
  bash ./configure
  make prefix=. install
  assertEquals "Performing './configure' and 'make install' on $MAKE_DIR/gzip-1.2.4" 0 $?
  popd
  pushd $MAKE_DIR/iozone3_397/src/current/
  make clean
  make linux
  assertEquals "Performing 'make clean' and 'make linux' on $MAKE_DIR" 0 $?
  popd

  echo Contents of make directory...
  ls $MAKE_DIR
}

testUnmountFuse()
{
  sync
  if fuse_supported; then
    sleep 30  # gffs cache clearance.
    echo $(date): before unmounting
    fusermount -u $MOUNT_POINT &>/dev/null
    assertEquals "Unmounting fuse mount" 0 $?
    rmdir $MOUNT_POINT
    assertEquals "Removing fuse mount directory" 0 $?
  fi
}

testCleaningMakeDir()
{
  if ! fuse_supported; then return 0; fi
  echo $(date): before removing the make directory
  grid rm -rf $RNSPATH/$MAKE_DIR_SUFFIX
  assertEquals "Removing the $MAKE_DIR_SUFFIX under $RNSPATH" 0 $?
  echo $(date): after removing the make directory
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

