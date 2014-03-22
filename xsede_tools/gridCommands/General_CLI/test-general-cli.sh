#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

## This script runs all the General grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

testHistoryCommand()
{
  grid script local:./gridCmd_history.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'history' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testPingCommand()
{
  grid script local:./gridCmd_ping.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'ping' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testPwdCommand()
{
  grid script local:./gridCmd_pwd.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'pwd' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testScheduleTerminationCommand()
{
  grid script local:./gridCmd_schedule-termination.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'schedule_termination' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

#NOT WORKING
xtestSetUserConfigCommand() 
{
  grid script local:./gridCmd_set-user-config.xml $GENII_INSTALL_DIR
  retval=$?
  assertEquals "Testing 'set-user-config' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

#NOT WORKING
xtestSetResourcePropertiesCommand() 
{
  grid script local:./gridCmd_set-resource-properties.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'set-resource-properties' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

#NOT WORKING
xtestSetCommand()
{
  grid script local:./gridCmd_set.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'set' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

oneTimeTearDown()
{

	echo
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

