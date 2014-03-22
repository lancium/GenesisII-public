#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# the number of files to create in the RNS directory.
MAX_FILES=1000

# how many files to copy up at once; this is a limiter for the size of the grid client's
# memory usage.  the full thousand at once does not currently fit in 512M.
COPY_CHUNK=100

# where we hook in the fuse mount.
export MOUNT_POINT=$WORKDIR/mount-largeRNS
# the user's home directory from fuse perspective.
export HOME_DIR=$MOUNT_POINT/$RNSPATH

export BIGDIRNAME=huge_dir
 
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

  test_fuse_mount $MOUNT_POINT
  check_if_failed "Mounting grid to local directory"
}

# creates all of the files for the test in the rns path.
function copyFilesUp()
{
  start_time="$(date +%s)"
  for (( i = 1 ; i <= $MAX_FILES; i++ )); do
    echo blahHumbug$RANDOM$RANDOM >$HOME_DIR/$BIGDIRNAME/file_instance_$i.txt
    if [ $? -ne 0 ]; then
      echo "error"
      return 1
    fi
  done
  end_time="$(date +%s)"
  echo "$(($end_time - $start_time))"
  return 0
}

testCreatingLargeDirectory()
{
  if ! fuse_supported; then return 0; fi

  echo "Creating files starts at $(date)"

  # clean up any prior version of the test directory.
  \rm -rf $HOME_DIR/$BIGDIRNAME

  # recreate our target directory in the grid.
  mkdir $HOME_DIR/$BIGDIRNAME
  assertEquals "Making test folder $HOME_DIR/$BIGDIRNAME" 0 $?

  # make the files in the target directory.
  copy_time=$(copyFilesUp)
  assertEquals "Creating test files in $HOME_DIR/$BIGDIRNAME" 0 $?
  
  echo "Time taken to copy $MAX_FILES to grid is ${copy_time}s"

  echo "Creating and copying all files done at $(date)"
}

testScanningLargeDirectory()
{
  if ! fuse_supported; then return 0; fi

  # snooze to allow the cache to empty out.
  sleep 30

  timed_grid ls $RNSPATH/$BIGDIRNAME
  assertEquals "Run ls on new directory" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to list $BIGDIRNAME: $real_time s"

  timed_grid ls $RNSPATH/$BIGDIRNAME
  assertEquals "Run ls on new directory again" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to list $BIGDIRNAME after cached: $real_time s"

  # try listing the same directory but with a simple pattern as a filter.
  timed_grid ls $RNSPATH/$BIGDIRNAME/*5*
  assertEquals "Run ls on new directory with pattern" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to scan $BIGDIRNAME for files with simple pattern: $real_time s"
}

testCleaningOutBigDir()
{
  if ! fuse_supported; then return 0; fi

  oneTimeTearDown  # leverage cleanup again.
  assertEquals "Clean up directory with $MAX_FILES entries" 0 $?
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to clean up $BIGDIRNAME: $real_time s"
}

oneTimeTearDown()
{
  if ! fuse_supported; then return 0; fi

  fusermount -u $MOUNT_POINT &>/dev/null
  sync ; sleep 2
  if [ -d $MOUNT_POINT ]; then
    rmdir $MOUNT_POINT
  fi

  grid ls -d $RNSPATH/$BIGDIRNAME &>/dev/null
  if [ $? -eq 0 ]; then
    # seems like this directory does exist in the grid.  let's remove it.
    timed_grid rm -r $RNSPATH/$BIGDIRNAME 
  fi
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

