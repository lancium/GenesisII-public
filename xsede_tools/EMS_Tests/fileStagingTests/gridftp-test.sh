#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "Copying necessary file to Grid namespace"
  grid cp local:./cat.sh grid:$RNSPATH
  grid cp local:./hostname.sh grid:$RNSPATH
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testGRIDFTP()
{
  for i in $available_resources; do
    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-gridftp.jsdl $i
    assertEquals "Submitting single cat job with file staging using GRIDFTP protocol" 0 $?
    grep -i "Job Submitted" $GRID_OUTPUT_FILE
  done
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "Pending ftp jobs should complete without errors." 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

