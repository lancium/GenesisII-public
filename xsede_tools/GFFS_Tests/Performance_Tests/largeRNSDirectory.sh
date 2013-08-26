#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# the number of files to create in the RNS directory.
MAX_FILES=1000

# how many files to copy up at once; this is a limiter for the size of the grid client's
# memory usage.  the full thousand at once does not currently fit in 512M.
COPY_CHUNK=100

# where we hook in the fuse mount.
MOUNT_POINT=$WORKDIR/mount-largeRNS
# the user's home directory from fuse perspective.
HOME_DIR=$MOUNT_POINT/$RNSPATH
 
oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  if ! fuse_supported; then return 0; fi

  # leverage our cleanup from tear down.
  oneTimeTearDown

  mkdir $MOUNT_POINT
}

testMounting()
{
  if ! fuse_supported; then return 0; fi

  echo "Mounting $MOUNT_POINT"
  fuse --mount local:$MOUNT_POINT
  sleep 30
  checkMount="$(mount)"
#echo checkmount is: $checkMount
#echo mount point seeking is: $MOUNT_POINT
  retval=1
  if [[ "$checkMount" =~ .*$MOUNT_POINT.* ]]; then retval=0; fi
  assertEquals "Mounting to local directory" 0 $retval
  if [ $retval == 0 ]; then
    ls -l $MOUNT_POINT
    assertEquals "Can list the fuse mounted directory" 0 $retval
  else
    rmdir $MOUNT_POINT
    fail "Failed to mount the GFFS mount point, bailing."
    exit 1
  fi
}

# makes all of the files in the rns path.  we want this as a single
# function so we can time it.
copyFilesUp()
{
  for (( i = 1 ; i <= $MAX_FILES; i++ )); do
    echo blahHumbug$RANDOM$RANDOM >$HOME_DIR/big_dir/file_instance_$i.txt
    if [ $? -ne 0 ]; then
      return 1
    fi
  done
}

testCreatingLargeDirectory()
{
  if ! fuse_supported; then return 0; fi

  echo "Creating files starts at $(date)"

  # recreate our target directory in the grid.
  mkdir $HOME_DIR/big_dir
  assertEquals "Making test folder $HOME_DIR/big_dir" 0 $?

  # make the files in the target directory.
  copy_time=$(time -p copyFilesUp | awk '{print $2}')
  echo "Time taken to copy $MAX_FILES to grid is ${copy_time}s"

  echo "Creating and copying all files done at $(date)"
}

testScanningLargeDirectory()
{
  if ! fuse_supported; then return 0; fi

  # snooze to allow the cache to empty out.
  sleep 30

  timed_grid ls $RNSPATH/big_dir
  assertEquals "Run ls on new directory" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to list big_dir: $real_time s"

  timed_grid ls $RNSPATH/big_dir
  assertEquals "Run ls on new directory again" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to list big_dir after cached: $real_time s"

## ls doesn't support patterns??
#  timed_grid ls $RNSPATH/big_dir/*5*
#  assertEquals "Run ls on new directory with pattern" 0 $?
#  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
#  echo "Time taken to scan big_dir for files with pattern: $real_time s"

}

testCleaningOutBigDir()
{
  if ! fuse_supported; then return 0; fi

  oneTimeTearDown  # leverage cleanup again.
  assertEquals "Clean up directory with $MAX_FILES entries" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to clean up big_dir: $real_time s"
}

oneTimeTearDown()
{
  if ! fuse_supported; then return 0; fi

  fusermount -u $MOUNT_POINT &>/dev/null
  sync ; sleep 2
  if [ -d $MOUNT_POINT ]; then
    rmdir $MOUNT_POINT
  fi

  grid ls -d $RNSPATH/big_dir &>/dev/null
  if [ $? -eq 0 ]; then
    # seems like this directory does exist in the grid.  let's remove it.
    timed_grid rm -r $RNSPATH/big_dir 
  fi
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

