#!/bin/bash

# Author: Vanamala Venkataswamy
# mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# the file name we'll use for our large staging.
HUGE_TEST_FILE=$TEST_TEMP/randomhalfgig.dat

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "creating 5KB file..."
  createRandomFile "$TEST_TEMP/random5KB.dat" 5120

  echo "creating large file for staging, this may take a couple minutes..."
  createRandomFile "$HUGE_TEST_FILE" $((1048576 * 64))
}

testCopyFilesToGFFS()
{
  echo "Copying bash input file to GFFS"
  grid cp local:$PWD/mv-files.sh grid:$RNSPATH
  exitCode=$?
  result=`expr $exitCode`
  assertEquals "Copied bash input file" 0 $exitCode
  echo "Copying 5KB file to GFFS"
  grid cp local:$TEST_TEMP/random5KB.dat grid:$RNSPATH
  exitCode=$?
  result=`expr $exitCode + $result`
  assertEquals "Copied 5KB file" 0 $exitCode
  echo "Copying 64MB file to GFFS"
  grid cp local:$HUGE_TEST_FILE grid:$RNSPATH/randomhalfgig.dat
  exitCode=$?
  result=`expr $exitCode + $result`
  assertEquals "Copied 64MB file" 0 $exitCode
  if [ $result -ne 0 ]; then
    echo "Copying input files to GFFS failed, failing test.."
    exit 1
  fi
}

testFileStagingPerformance()
{
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/stage-100-files-5KB.jsdl
  assertEquals "Submitting ls job with 100 5KB stage-ins" 0 $?

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/stage-1-file-halfgig.jsdl
  assertEquals "Submitting ls job with a half GB stage-in/stage-out" 0 $?
  echo `date`": jobs submitted"
}

testJobStatus()
{
  # assume 20 minutes allowed for this copy, if they haven't said it should
  # get more time to complete.
  if [ -z "$QUEUE_SLEEP_DURATION" ]; then
    QUEUE_SLEEP_DURATION=30
  fi
  local mins20=$((20 * 60 / $QUEUE_SLEEP_DURATION))
  if [ -z "$QUEUE_TRIES_ALLOWED" ]; then export QUEUE_TRIES_ALLOWED=$mins20; fi
  if [ $QUEUE_TRIES_ALLOWED -lt 90 ]; then export QUEUE_TRIES_ALLOWED=$mins20; fi

  # just wait, without removing jobs, so we can test results.
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No jobs should be left" 0 $?
}

testFileConsistency()
{
#  # sleep proportionally to size of big file, since we are seeing job stage out happens after
#  # the job is marked as finished in the queue.
#  filesize=$(ls -al $HUGE_TEST_FILE | awk '{print $5}' )
##echo full file size is $filesize
#  sleepytime=$(expr $filesize / 1024 / 1024 / 3 + 30)
#  echo snoozing for $sleepytime seconds to await job stage outs.
#  sleep $sleepytime
#
  echo contents of testing path after job is complete:
  grid ls -al $RNSPATH
  cat $GRID_OUTPUT_FILE

  grid cp $RNSPATH/random5KB.transferred local:$TEST_TEMP/random5KB.transferred
  diff $TEST_TEMP/random5KB.dat $TEST_TEMP/random5KB.transferred
  assertEquals "Checking File consistency, local copy vs copy on grid namespace" 0 $?

  grid cp $RNSPATH/randomhalfgig.transferred local:$TEST_TEMP/randomhalfgig.transferred
  diff $HUGE_TEST_FILE $TEST_TEMP/randomhalfgig.transferred
  retval=$?
  assertEquals "Checking File consistency, local copy vs copy on grid namespace" 0 $retval
  if [ $retval -ne 0 ]; then
    echo here is list of the two files that differ:
    ls -al $HUGE_TEST_FILE $TEST_TEMP/randomhalfgig.transferred
    echo here are md5sums of failed files:
    md5sum $HUGE_TEST_FILE $TEST_TEMP/randomhalfgig.transferred
  fi
  echo `date`": test ended"

  # now clean out all the jobs.
  wait_for_all_pending_jobs $QUEUE_PATH whack
}

oneTimeTearDown()
{
  rm -f $TEST_TEMP/random5KB.* $TEST_TEMP/randomhalfgig.*
  grid rm $RNSPATH/random5KB.dat
  grid rm $RNSPATH/random5KB.transferred
  grid rm $RNSPATH/randomhalfgig.dat
  grid rm $RNSPATH/randomhalfgig.transferred
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

