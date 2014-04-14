#!/bin/bash
#
# tries a few queue submissions with a larger payload which is used for both
# input and output staging.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# how large to make the file that will be staged in, uuencoded and staged
# back out, in megabytes.
#STAGE_IN_SIZE=16
STAGE_IN_SIZE=340

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "Copying necessary files to Grid namespace"
  grid cp local:./large-chunks.sh grid:$RNSPATH

  # check if we already have a staging file.
  grid ls grid:$RNSPATH/large-chunk.dat
  if [ $? -eq 0 ]; then
    echo Using existing stage-in file:
    grid ls -al grid:$RNSPATH/large-chunk.dat
    cat $GRID_OUTPUT_FILE
  else
    echo Need to create a stage-in file...
    echo "Generating largish payload"
    dd if=/dev/urandom of=large-chunk.dat bs=1048576 count=$STAGE_IN_SIZE
    echo Copying large stage-in file to grid...
    grid cp local:./large-chunk.dat grid:$RNSPATH
  fi
  echo Done setting up large payload test.
}

testQueueSubmitManyWithSleep()
{
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/large-chunk-try4.jsdl
  assertEquals "Submitting many 'large-chunks' jobs" 0 $?
}

testWaitForSleepers()
{
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "No sleeping jobs should be left" 0 $?
}

oneTimeTearDown()
{
  # clean up inside grid.
  grid rm $RNSPATH/encoded-out*.txt $RNSPATH/largey-error*.txt $RNSPATH/large-chunks.sh
  # note that we leave the large-chunk.dat file in the RNSPATH for future use, but clean
  # up the local chunky file.  and if the file was already in the grid, we won't be able
  # to clean it locally, so we hide error output.
  \rm large-chunk.dat 2>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

