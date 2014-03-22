#!/bin/bash


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

  echo `date`": Creating parent directory for links to jobs: $RNSPATH/jobs"
  grid mkdir grid:$RNSPATH/jobs

}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testBESPerformance()
{
    for i in $available_resources; do
        local shortbes=$(basename $i)
    	echo `date`": Creating directory for links to jobs: $RNSPATH/jobs/$shortbes"
    	grid mkdir grid:$RNSPATH/jobs/$shortbes
    	echo `date`": Submitting 1000 sleep jobs to $shortbes"
        time grid script local:./sleep-bes-submit-1000.xml $i $RNSPATH/jobs/$shortbes
        assertEquals "Submitting 1000 sleep jobs" $? 0
        if [ $? == 0 ]
        then
        	echo `date`": 1000 jobs submitted"
       	fi
    done
}

testCancelJobs()
{
   for i in $available_resources; do
        local shortbes=$(basename $i)
	grid terminate-activities --bes=$i --activityFolders=$RNSPATH/jobs/$shortbes
	if [ $? == 0 ]
       	then
		echo `date`": 1000 jobs cancelled"
       	fi
   done
}

oneTimeTearDown()
{
  for i in $available_resources; do
    local shortbes=$(basename $i)
    echo `date`": Cleaning up job links at $RNSPATH/jobs/$shortbes"
    grid rm -rf $RNSPATH/jobs/$shortbes
  done
  grid rm -rf $RNSPATH/jobs
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

