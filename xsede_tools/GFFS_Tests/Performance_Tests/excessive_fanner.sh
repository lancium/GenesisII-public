#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# where we will fuse mount the grid locally.
export MOUNT_POINT="$WORKDIR/mount-directoryTree"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # remove any previous mount point.
  fusermount -u "$MOUNT_POINT" &>/dev/null
  if [ -e "$MOUNT_POINT" ]; then rmdir "$MOUNT_POINT"; fi
  # take out prior exports too.
  grid ls ${RNSPATH}/export-local &>/dev/null
  if [ $? == 0 ]; then
    grid export --quit ${RNSPATH}/export-local &>/dev/null
    grid unlink ${RNSPATH}/export-local
    if [ $? -ne 0 ]; then
      echo "The grid unlink attempt on ${RNSPATH}/export-local failed although we"
      echo "believe the file is present.  This is not a good sign."
    fi
  fi
  # trash any older test directories that were left lying around.
  rm -rf testDir
  grid rm -rf ${RNSPATH}/testDir &>/dev/null
  # create a new mount point.
  mkdir "$MOUNT_POINT"
}

testFuseMount () {
  if ! fuse_supported; then return 0; fi
  # Create a grid mount point, mount the grid
echo mount point is $MOUNT_POINT
  fuse --mount local:"$MOUNT_POINT"
  sleep 20

  test_fuse_mount "$MOUNT_POINT"
  check_if_failed "Mounting grid to local directory"

  grid ls
  cat $GRID_OUTPUT_FILE
}

testCreateDirectory () {
  if ! fuse_supported; then return 0; fi
  fan_out_directories testDir 2 2 1
}

testFuseRecursiveCp() {
  if ! fuse_supported; then return 0; fi
  # Recursively copy files from $1 into the $2
  # Then ls -lR the directory and count the number of lines
  time cp -rv testDir "$MOUNT_POINT/$RNSPATH"
  assertEquals "Recursively copying from testDir to $MOUNT_POINT/$RNSPATH" 0 $?
echo "============== test start ================" >>$HOME/.GenesisII/grid-client.log
  time ls -lR "$MOUNT_POINT/$RNSPATH"
  assertEquals "Recursively listing testDir $MOUNT_POINT/$RNSPATh" 0 $?
  time ls -lR "$MOUNT_POINT/$RNSPATH"
  assertEquals "try #2 Recursively listing testDir $MOUNT_POINT/$RNSPATh" 0 $?
  time find "$MOUNT_POINT/$RNSPATH" -type d -exec ls -d {} ';'
  assertEquals "finding directories in testDir on $MOUNT_POINT/$RNSPATh" 0 $?
  time find "$MOUNT_POINT/$RNSPATH" -type d -exec ls -d {} ';'
  assertEquals "try #2 finding directories in testDir on $MOUNT_POINT/$RNSPATh" 0 $?
echo "=============== test end =================" >>$HOME/.GenesisII/grid-client.log
}

testRemovingTestDir()
{
  if ! fuse_supported; then return 0; fi
  grid rm -r ${RNSPATH}/testDir
  assertEquals "cleaning up test directory" 0 $?
}

oneTimeTearDown() {
  fusermount -u "$MOUNT_POINT" &>/dev/null
  rmdir "$MOUNT_POINT"
  rm -rf testDir
  grid export --quit $RNSPATH/export-local &>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

