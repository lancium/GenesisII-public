#!/bin/bash

##Author: Vanamala Venkataswamy

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

  export TIMEFORMAT='%3lR'

}

testQueuePerformanceSubmission()
{
  
  time grid script local:./ls-submit-1.xml $QUEUE_PATH
  assertEquals "Timing - Submitting 1 'ls' jobs, not parameter sweep" 0 $?

  time grid script local:./ls-submit-10.xml $QUEUE_PATH
  assertEquals "Timing - Submitting 10 'ls' jobs, not parameter sweep" 0 $?

  time grid script local:./ls-submit-100.xml $QUEUE_PATH
  assertEquals "Timing - Submitting 100 'ls' jobs, not parameter sweep" 0 $?

  time grid script local:./ls-submit-1000.xml $QUEUE_PATH
  assertEquals "Timing - Submitting 1000 'ls' jobs, not parameter sweep" 0 $?
  echo `date`": jobs submitted"
}

testJobStatus()
{
  wait_for_all_pending_jobs $QUEUE_PATH
  assertEquals "No jobs should be left" 0 $?
}

function submit_script_and_time_killing_it()
{
  script_name="$1"; shift
  failures=0

  grid script $script_name $QUEUE_PATH 
  if [ $? -ne 0 ]; then ((failures++)); fi
  res=$(cat $GRID_OUTPUT_FILE | gawk '{print $5}' | sed 's/\(.*\)../\1/'| sed 's/.\(.*\)/\1/')
  echo -e "job tickets:\n$res"

  time grid qkill $QUEUE_PATH $res
  if [ $? -ne 0 ]; then ((failures++)); fi

  return $failures
}

# removed this test until the Genesis grid queue deals with cancellation errors
# in a reasonable fashion

#testQueuePerformanceCancel()
#{
#  submit_script_and_time_killing_it local:./sleep-submit-1.xml 
#  assertEquals "Timing - qkill 1 'sleep' jobs, not parameter sweep" 0 $? 
#  
#  submit_script_and_time_killing_it local:./sleep-submit-10.xml 
#  assertEquals "Timing - qkill 10 'sleep' jobs, not parameter sweep" 0 $?
#
#  submit_script_and_time_killing_it local:./sleep-submit-100.xml 
#  assertEquals "Timing - qkill 100 'sleep' jobs, not parameter sweep" $? 0
#
#  submit_script_and_time_killing_it local:./sleep-submit-1000.xml 
#  assertEquals "Timing - qkill 1000 'sleep' jobs, not parameter sweep" $? 0
#
#  grid qcomplete $QUEUE_PATH --all
#  assertEquals "Cleaning out jobs in queue after all are killed" $? 0
#
#  echo `date`": jobs killed"
#}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

