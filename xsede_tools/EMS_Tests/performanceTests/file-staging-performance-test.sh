#!/bin/bash

# Author: Vanamala Venkataswamy
# mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "creating 5KB file..."
  dd if=/dev/urandom of=random5KB.dat bs=1 count=5120

  HUGE_TEST_FILE=./random5GB.dat
  if [ ! -f "$HUGE_TEST_FILE" ]; then
    # to speed up continuous integration builds, we will re-use the following
    # file if it's already available.  only defect is if it isn't actually 5
    # gigs.  but this should not be an issue for people doing normal testing.
    local PREMADE_FILE=$TMP/premade_5gig.dat
    if [ -f $PREMADE_FILE ]; then
      echo "using a pre-constructed file for 5gigs example: $PREMADE_FILE" 
      HUGE_TEST_FILE="$PREMADE_FILE"
    else
      echo "creating 5GB file, this may take a few minutes..."
      dd if=/dev/urandom of=$HUGE_TEST_FILE bs=1048576 count=5120
    fi
  fi
}

testCopyFilesToGFFS()
{
  echo "Copying bash input file to GFFS"
  grid cp local:./mv-files.sh grid:$RNSPATH
  exitCode=$?
  result=`expr $exitCode`
  assertEquals "Copied bash input file" 0 $exitCode
  echo "Copying 5KB file to GFFS"
  grid cp local:./random5KB.dat grid:$RNSPATH
  exitCode=$?
  result=`expr $exitCode + $result`
  assertEquals "Copied 5KB file" 0 $exitCode
  echo "Copying 5GB file to GFFS"
  grid cp local:$HUGE_TEST_FILE grid:$RNSPATH/random5GB.dat
  exitCode=$?
  result=`expr $exitCode + $result`
  assertEquals "Copied 5GB file" 0 $exitCode
  if [ $result -ne 0 ]
  then
    echo "Copying input files to GFFS failed, failing test.."
    exit 1
  fi

}

testFileStagingPerformance()
{
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/stage-100-files-5KB.jsdl
  assertEquals "Submitting ls job with 100 5KB stage-ins" 0 $?

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/stage-1-file-5GB.jsdl
  assertEquals "Submitting ls job with a 5GB stage-in/stage-out" 0 $?
  echo `date`": jobs submitted"
}

testJobStatus()
{
  # assume 20 minutes allowed for this copy, if they haven't said it should
  # get more time to complete.
  if [ -z "$QUEUE_SLEEP_DURATION" ]; then
    QUEUE_SLEEP_DURATION=120
  fi
  local mins20=$((20 * 60 / $QUEUE_SLEEP_DURATION))
  if [ -z "$QUEUE_TRIES_ALLOWED" ]; then export QUEUE_TRIES_ALLOWED=$mins20; fi
  if [ $QUEUE_TRIES_ALLOWED -lt 90 ]; then export QUEUE_TRIES_ALLOWED=$mins20; fi

  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No jobs should be left" 0 $?
}

testFileConsistency()
{
  grid cp $RNSPATH/random5KB.transferred local:./random5KB.transferred
  diff ./random5KB.dat ./random5KB.transferred
  assertEquals "Checking File consistency, local copy vs copy on grid namespace" 0 $?

  grid cp $RNSPATH/random5GB.transferred local:./random5GB.transferred
  diff $HUGE_TEST_FILE ./random5GB.transferred
  assertEquals "Checking File consistency, local copy vs copy on grid namespace" 0 $?
  echo `date`": test ended"
}

oneTimeTearDown()
{
  rm -f ./random5KB.*
  # we still remove based on the normal name, so we only clean up one we just created.
  rm -f ./random5GB.*
  grid rm $RNSPATH/random5KB.dat
  grid rm $RNSPATH/random5KB.transferred
  grid rm $RNSPATH/random5GB.dat
  grid rm $RNSPATH/random5GB.transferred
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

