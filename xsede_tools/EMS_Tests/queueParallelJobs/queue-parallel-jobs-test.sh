#!/bin/bash

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

  echo "Copying necessary file to Grid namespace"
  grid cp local:./hostname.sh grid:$RNSPATH
  grid cp local:./simple-mpi.c grid:$RNSPATH
  grid cp local:./parameter-sweep-mpi.c grid:$RNSPATH

}

testQueueParallelJobsSubmission()
{
  if [ "$BES_TYPE" = "Genesis" ]; then
  	echo "Skipping MPI jobs on Genesis BES."
  else
    grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/simple-mpi.jsdl
    assertEquals "Submitting simple MPI job - stageout output files" 0 $?

    grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/parameter-sweep-mpi.jsdl
    assertEquals "Submitting parameter sweep MPI job - stage-in and stage out files" 0 $?
    echo `date`": job submitted"
  fi
}

testWaitingOnJobs()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No jobs should be left" 0 $?
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

