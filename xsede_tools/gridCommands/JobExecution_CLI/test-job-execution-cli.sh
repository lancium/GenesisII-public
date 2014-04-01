#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

## This script runs all the Job Execution related grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

	grid ping $RNSPATH/test-queue 2>/dev/null 1>/dev/null
	retval=$?
	if [ $retval == 0 ]; then
		grid script local:./queue_bes_destroy.xml $RNSPATH
	fi
}

testQueueBESSetup()
{
	grid script local:./queue_bes_setup.xml $RNSPATH $CONTAINERPATH
	retval=$?
	assertEquals "Setting up test-queue and test-bes in $RNSPATH" 0 $retval
  	if [ $retval != 0 ]; then
    		fail "Bailing on rest of test because resources are missing."
    		exit 1
  	fi
}

testParseJSDLCommand()
{
  grid script local:./gridCmd_parseJSDL.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'parseJSDL' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQCompleteCommand()
{
  grid script local:./gridCmd_qcomplete.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'qcomplete' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQConfigureCommand()
{
  grid script local:./gridCmd_qconfigure.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'qconfigure' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQKillCommand()
{
  grid script local:./gridCmd_qkill.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'qkill' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQListCommand()
{
  grid script local:./gridCmd_qlist.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'qlist' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQQueryCommand()
{
  grid script local:./gridCmd_qquery.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'qquery' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQRescheduleCommand()
{
  grid script local:./gridCmd_qreschedule.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'qreschedule' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQStatCommand()
{
  grid script local:./gridCmd_qstat.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'qstat' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testQSubCommand()
{
  grid script local:./gridCmd_qsub.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'qsub' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testResourceHistoryCommand()
{
  grid script local:./gridCmd_resource-history.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'resource-history' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

#NOT WORKING
xtestRunJSDLCommand()
{
  grid script local:./gridCmd_runJSDL.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'runJSDL' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

#NOT WORKING
xtestRunCommand()
{
  grid script local:./gridCmd_run.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'run' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}


oneTimeTearDown()
{
        grid ping $RNSPATH/test-queue 2>/dev/null 1>/dev/null
        retval=$?
        if [ $retval == 0 ]; then
                grid script local:./queue_bes_destroy.xml $RNSPATH
        fi

	rm ./output.txt 2>/dev/null 1>/dev/null
	rm ./ls.binary 2>/dev/null 1>/dev/null

}


# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

