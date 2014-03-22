#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

## This script runs all the Miscellanious grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

#BUGGY, works the first time,and then gives NULL Pointer exception
xtestConnectCommand()
{
  grid script local:./gridCmd_connect.xml $RNSPATH $GENII_INSTALL_DIR
  retval=$?
  assertEquals "Testing 'connect' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testCreateResourceCommand()
{
  grid script local:./gridCmd_create-resource.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'create-resource' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testEchoCommand()
{
  grid script local:./gridCmd_echo.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'echo' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testTTYCommand()
{
  grid script local:./gridCmd_tty.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'tty' command and its options" 0 $retval
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

