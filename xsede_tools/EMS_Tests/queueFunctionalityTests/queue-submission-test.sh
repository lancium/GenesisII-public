#!/bin/bash

##Author; Vanamala Venkataswamy
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

  echo "Copying necessary files to Grid namespace"
  grid cp local:./cat.sh grid:$RNSPATH
  grid cp local:./hostname.sh grid:$RNSPATH
  grid cp local:./hostname-sleep.sh grid:$RNSPATH

}

testQueueSubmitSimple()
{
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/ls-single-job.jsdl
  assertEquals "Submitting single '/bin/ls' job" 0 $?

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/hostname-sleep-60s.jsdl
  assertEquals "Submitting single '/bin/hostname'; sleep 60 job" 0 $?

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/hostname-stage-out.jsdl
  assertEquals "Submitting '/bin/hostname' job, sleep for 60s, stage-out the output.txt" 0 $?

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/cat-stdin-stageout.jsdl
  assertEquals "Submitting single 'cat' job to cat a file, stage-out the output file" 0 $?
}

testWaitForSimpleJobs()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No simple jobs should be left" 0 $?
}

testQueueSubmitManyWithSleep()
{
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/hostname-parameter-sweep-100.jsdl
  assertEquals "Submitting 100 '/bin/hostname'; sleep 60 jobs" 0 $?
}

testWaitForSleepers()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No sleeping jobs should be left" 0 $?
}

testSubmitManyJobsParameterSweep()
{
echo "starting submit... $(date)"
  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/ls-parameter-sweep-1000.jsdl
  retval=$?
echo "ending submit... $(date)"
  assertEquals "Submitting 1000 '/bin/ls' jobs (parameter sweep)" 0 $retval
}

testWaitForManyJobStatusSweep()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No parameter sweep jobs should be left" 0 $?
}

testSubmitManyJobsScript()
{
echo "starting submit... $(date)"
  grid script local:./ls-submit-1000.xml $QUEUE_PATH
  retval=$?
echo "ending submit... $(date)"
  assertEquals "Submitting 1000 'ls' jobs, not parameter sweep" 0 $retval
}

testWaitForManyJobScripted()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No scripted thousand jobs should be left" 0 $?
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

