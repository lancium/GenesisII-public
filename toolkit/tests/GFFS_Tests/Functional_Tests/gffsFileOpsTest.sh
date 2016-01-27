#!/bin/bash

# Author: Vanamala Venkataswamy
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

MOUNT_POINT="$TEST_TEMP/mount-gffsFileOps"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # clean up existing mount; we just try to unmount it without checking folder, since we sometimes
  # get a strange result of the folder being reported as non-existent, even though there's still
  # a dead fuse mount on it.
  fusermount -u "$MOUNT_POINT" &>/dev/null
  if [ -e "$MOUNT_POINT" ]; then
	rmdir "$MOUNT_POINT"
  fi 
}

testCompilingApp()
{
  echo "Current working directory is $(\pwd)"

  which make
  if [ $? == 0 ]; then
	make 
	assertEquals "building test in C" 0 $?
  else
	which gmake
	if [ $? == 0 ]; then
        	gmake
		assertEquals "building test in C" 0 $?
	else
		echo "make/gmake not found, cannot proceed. Exiting..."
		assertEquals "make tool not found" 0 1
	fi
  fi
}

testFuseMounting()
{
  if ! fuse_supported; then return 0; fi
  mkdir "$MOUNT_POINT"
  echo Making new fuse mount.
  fuse --mount local:"$MOUNT_POINT"
  sleep 20

  test_fuse_mount "$MOUNT_POINT"
  check_if_failed "Mounting grid to local directory"
}

testFileOperations()
{
  if ! fuse_supported; then return 0; fi
  $TEST_TEMP/gffs_file_ops_test "${MOUNT_POINT}${RNSPATH}"
  assertEquals "File operations test" 0 $?
}

testRemovingFuseMount()
{
  if ! fuse_supported; then return 0; fi
  grid fuse --unmount local:"$MOUNT_POINT"
  assertEquals "Unmount grid mounted folder" 0 $?
  sleep 5
  rmdir "$MOUNT_POINT"
  assertEquals "Remove mounted folder" 0 $?
}

oneTimeTearDown()
{
  \rm $TEST_TEMP/gffs_file_ops_test
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

