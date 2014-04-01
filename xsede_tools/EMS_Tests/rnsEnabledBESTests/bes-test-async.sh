#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

if [ "$BES_TYPE" != "Genesis" ]; then
  echo "This test only runs against a Genesis BES; you currently have a Unicore BES configured."
  exit 0
fi

TEST_SETS_PER_RUN=1  # how many copies of each job should run?
#hmmm: if this is set to more than one, we see collisions on the output file
#      resulting in exceptions in the log.
#      we need to be able to add a random component to the names that is
#      kept properly within the same file (since we have to specify output file
#      twice in there).

# make these files unique so that we can run multiple instances of this test.
TESTING_INSIDE_RNS_PATH="$RNSPATH/bes-test-async"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # allocate any random ids we intend to use.
#  setup_random_id host-stageout
#echo "got rando for host-stageout of '$(get_random_id host-stageout)'"

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
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

# makes sure we can submit the jobs to the RNS output path.
testGiveBESPermissionsToPath()
{
  for i in $available_resources; do
    grid chmod "$RNSPATH" +rwx "$i"
    assertEquals "Giving resource '$i' access to RNS path" 0 $?
  done
}

testRunningJobsAsynchronously()
{
  local i
  local j

  for i in $available_resources; do
    for ((j = 0; j < $TEST_SETS_PER_RUN; j++)); do
      local shortbes=$(basename $i)
      submit_asynchronous_job_on_BES "$TESTING_INSIDE_RNS_PATH/$shortbes-ls-single-job.$RANDOM-$RANDOM" "ls-single-job.jsdl" "$i"
      submit_asynchronous_job_on_BES "$TESTING_INSIDE_RNS_PATH/$shortbes-hostname-sleep-60s.$RANDOM-$RANDOM" "hostname-sleep-60s.jsdl" "$i"
      submit_asynchronous_job_on_BES "$TESTING_INSIDE_RNS_PATH/$shortbes-cat-stdin-stageout.$RANDOM-$RANDOM" "cat-stdin-stageout.jsdl" "$i"
      submit_asynchronous_job_on_BES "$TESTING_INSIDE_RNS_PATH/$shortbes-hostname-stage-out.$RANDOM-$RANDOM" "hostname-stage-out.jsdl" "$i"
    done
  done
}

testPollStatusOfQueue()
{
  poll_job_dirs_until_finished ${ASYNCHRONOUS_BES_PENDING_JOBS[*]}
  assertEquals "All jobs were concluded" 0 $?
}

oneTimeTearDown()
{
  echo "Leaving test $(basename $0)..."

  # note that we do not clean up the stage-in files, because we want to be
  # able to run multiple instances of this test, and that would trash the
  # other instances' stage-ins.
#hmmm: it would be nice if we could still clean up, by making more places in
#      the jsdl to put the stage-in paths into place.  currently, it just always
#      replaces the PATH with our $RNSPATH.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

