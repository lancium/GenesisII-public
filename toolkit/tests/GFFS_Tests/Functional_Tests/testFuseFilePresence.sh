#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

source "../../../prepare_tools.sh" "../../../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The xsede tool suite could not be automatically located.
  exit 1
fi

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 1; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# how long should we snooze before checking that fuse has gotten the
# notification that new files are available?
# ASG - 4 seconds is short, we are changing to 30. But, we really need to check the cache coherence code
FUSE_SNOOZE_TIME=5
# how many times we should test that fuse sees a new file in the grid.
#FUSE_COPY_COUNT=1
FUSE_COPY_COUNT=20

# where we hook in the fuse mount.
MOUNT_POINT="$TEST_TEMP/mount-gffsFuseCommands"
# the user's home directory from fuse perspective.
HOME_DIR="$MOUNT_POINT/$RNSPATH"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  if ! fuse_supported; then return 0; fi

  # need to just go for it and try to unmount; the directory left for a dead mount
  # is still present but is not seen by checks.
  fusermount -u "$MOUNT_POINT" &>/dev/null
  sync ; sleep 2
  if [ -d "$MOUNT_POINT" ]; then
    rmdir "$MOUNT_POINT"
  fi
  mkdir "$MOUNT_POINT"
}

testFuseMounting()
{
  if ! fuse_supported; then return 0; fi

  echo "Testing 'fuse --mount' command"
  echo "Mounting $MOUNT_POINT using Fuse Mount command"
  fuse --mount local:"$MOUNT_POINT"
  sleep 30

  test_fuse_mount "$MOUNT_POINT"
  check_if_failed "Mounting grid to local directory"
}

# this test just tries to make sure that the fuse driver eventually notices
# that a new file has been added to a directory.  'eventually' here is
# expected to be a very short time when notifications are enabled.  the
# test also verifies that the removal of the file is also noticed.
testNewFileInGridShowsUp()
{
  if ! fuse_supported; then return 0; fi
  local newfile=new-file-testing-fuse-yo
#hmmm: would be good to try the test from fuse perspective also, since below is changing only in grid.
  for ((i=0; i < $FUSE_COPY_COUNT; i++)); do
    local currfile=$newfile-$i
    grid echo this is a new file in the grid. \\\> $RNSPATH/$currfile
    assertEquals "Writing a new file in the grid" 0 $?
    # pause needed since fuse must see notification over network before it updates cache.
    sleep $FUSE_SNOOZE_TIME
    ls -1 "$HOME_DIR" | grep $currfile
    assertEquals "Testing that newly created file shows up in fuse directory listing" 0 $?
    grid rm $RNSPATH/$currfile
    assertEquals "Cleaning up newly created file" 0 $?
    # pause needed since fuse must see notification over network before it updates cache.
    sleep $FUSE_SNOOZE_TIME
    ls -1 "$HOME_DIR" | grep $currfile
    assertNotEquals "Testing that newly deleted file is missing in fuse directory listing" 0 $?
  done
}

testUnmountingFuseMount()
{
  if ! fuse_supported; then return 0; fi
  cd "$WORKDIR"  # change dir off fuse mount.
  grid fuse --unmount local:"$MOUNT_POINT"
  retval=$?
  sleep 30
  assertEquals "Testing 'fuse --unmount' command" 0 $retval
  rmdir "$MOUNT_POINT"
  assertEquals "Checking that directory is no longer mounted" 0 $?
}

oneTimeTearDown()
{
  echo fuse job:
  jobs
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

