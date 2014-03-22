#!/bin/bash

# Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

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

testRunningSynchronousJobs()
{
  for i in $available_resources; do
    echo "Submitting jobs on resource $(basename $i)..."
    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/ls-single-job.jsdl $i
    assertEquals "Submitting single '/bin/ls' job on $i" 0 $?

    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/hostname-sleep-60s.jsdl $i
    assertEquals "Submitting single '/bin/hostname' job, sleep for 60s on $i" 0 $?

    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-stdin.jsdl $i
    assertEquals "Submitting single 'cat' job to cat a file on $i" 0 $?

    grid cp "$RNSPATH/cat.out" local:"$TEST_TEMP/cat.out"
    assertEquals "Copying stage out file after job completion" 0 $?
    echo "Done with resource $(basename $i)."
  done
}

testCleaningUp()
{
  grid rm "$RNSPATH/cat.out" "$RNSPATH/cat.err"
  assertEquals "Removing staging files" 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

