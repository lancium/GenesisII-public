#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../../prepare_tests.sh ../../prepare_tests.sh 
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# this should point at a data file _in the grid_ of the appropriate size
# for testing transfer rates.
LARGE_DATA_FILE=/home/fred/jurgis-rns/gutbuster.dat

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

testTimeCopyingGridFileToLocal()
{
  local tmpfile=$(mktemp $TEST_TEMP/filesize.XXXXXXX)
  # get the size of our target file.
  logged_grid $tmpfile ls -al rns:$LARGE_DATA_FILE 
  assertEquals "Checking data file $LARGE_DATA_FILE exists." 0 $?
  # extract the size from the printout we made.
  local actual_size=$(awk '{print $1}' <$tmpfile)
  local readable_size="$(expr $actual_size / 1024 / 1024)"
  # now actually copy the file.
  echo "Now copying file $LARGE_DATA_FILE ($readable_size MB) to local file..."
  local fat_junk=$(mktemp $TEST_TEMP/tempcopy.XXXXXXX)
  timed_grid cp grid:$LARGE_DATA_FILE local:$fat_junk
  assertEquals "Copying file $LARGE_DATA_FILE ($readable_size MB) to local file" 0 $?
  # clean up the huge file now.
  \rm $fat_junk
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer $readable_size MB file : $real_time s"
  showBandwidth $real_time $actual_size
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

