#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

if [ "$BES_TYPE" != "Genesis" ]; then
  echo "This test only runs against a Genesis BES; you currently have a Unicore BES configured."
  exit 0
fi

TESTING_INSIDE_RNS_PATH="$RNSPATH/bes-test-async"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi
}

testUploadingStagingData()
{
  # copy up the files that will be staged in.
  # pattern is to leave out test which begins with 'b'...
  local i
  for i in [ac-z]*sh; do
    grid cp local:./$i grid:$RNSPATH/$i
    assertEquals "Copying up grid resource $i" 0 $?
  done
  # do not care about success of this, given multiple instances.
  grid mkdir $TESTING_INSIDE_RNS_PATH &>/dev/null
  # test that we can use our stage-out path.
  grid cp local:./$i grid:$TESTING_INSIDE_RNS_PATH/$i.$RANDOM.$RANDOM
  assertEquals "Copying file into stage-out folder" 0 $?
}

testJobStartedViaCpToSubmissionPoint()
{
  local i
  local j

  # we've got one simple job we'll add to the queue's submission point.
  grid cp local:./date-single-job.jsdl grid:$QUEUE_PATH/submission-point
  assertEquals "Submitted short-lived job by copying to queue's submission point" 0 $?

  # now we retrieve that guy so we can ice him and his folder (and possibly others).
  get_job_list_from_queue
  # clean out every job of ours that we know of.
  for i in ${SUBMISSION_POINT_JOB_LIST[*]}; do
    grid rm -rf $QUEUE_PATH/jobs/mine/all/$i
    assertEquals "Whacking job $i by removing job folder" 0 $?
  done

  # now we'll actually submit it and let it hang out.
  grid cp local:./date-single-job.jsdl grid:$QUEUE_PATH/submission-point
  assertEquals "Submitted long-lived job by copying to queue's submission point" 0 $?

  drain_my_jobs_out_of_queue
}

testCleaningUpRNSPath()
{
  grid rm -rf $TESTING_INSIDE_RNS_PATH
  assertEquals "Clean up the RNS folder '$TESTING_INSIDE_RNS_PATH'" 0 $?
  grid ls $TESTING_INSIDE_RNS_PATH &>/dev/null
  assertNotEquals "Folder should have disappeared." 0 $?
}

oneTimeTearDown()
{
  echo "Leaving test $(basename $0)..."
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

