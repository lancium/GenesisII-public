#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-11-2012

## This script runs all the ADMINISTRATON related grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

}

testChmodCommand()
{
  grid script local:./gridCmd_chmod.xml $RNSPATH test2 test2
  retval=$?
  assertEquals "Testing 'chmod' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}


testCreateUserDelegateCommand()
{
  grid script local:./gridCmd_create-user-delegate.xml $CONTAINERPATH test2 test2 test3 
  retval=$?
  assertEquals "Testing 'create-user-delegate' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

xtestGetAttributesCommand()
{
  grid script local:./gridCmd_get-attributes.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'get-attributes' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

xtestGetBESAttributesCommand()
{
  grid script local:./gridCmd_get-bes-attributes.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'get-bes-attributes' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

xtestMatchingParamsCommand()
{
  grid script local:./gridCmd_matching-params.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'matching-params' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

xtestMintEPRCommand()
{
  grid script local:./gridCmd_mint-epr.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'mint-epr' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

xtestSetContainerServicePropertiesCommand()
{
  grid script local:./gridCmd_set-container-service-properties.xml $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'set-container-service-properties' command and its options" 0 $retval
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

