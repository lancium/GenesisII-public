#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

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

testAttachHostCommand()
{
  grid script local:./gridCmd_attach-host.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'attach-host' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}


testBESPolicyCommand()
{
  grid script local:./gridCmd_bes-policy.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'bes-policy' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testGetAttributesCommand()
{
  grid script local:./gridCmd_get-attributes.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'get-attributes' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testGetBESAttributesCommand()
{
  grid script local:./gridCmd_get-bes-attributes.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'get-bes-attributes' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testMatchingParamsCommand()
{
  grid script local:./gridCmd_matching-params.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'matching-params' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testMintEPRCommand()
{
  grid script local:./gridCmd_mint-epr.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'mint-epr' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testSetContainerServicePropertiesCommand()
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

