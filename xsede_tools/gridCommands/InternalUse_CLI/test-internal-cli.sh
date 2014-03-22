#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

## This script runs all the Internal grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

testContainerStatsCommand()
{
  grid script local:./gridCmd_container-stats.xml $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'container-stats' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testGetUserDirCommand()
{
  grid script local:./gridCmd_GetUserDir.xml 
  retval=$?
  assertEquals "Testing 'GetUserDir' command and its options" 0 $retval
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

