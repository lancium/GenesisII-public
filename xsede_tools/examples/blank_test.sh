#!/bin/bash

# Test: X
# Author: Y

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

# this needs to be relative to where the test will actually reside; the ../../../../../etc
# should get to the top of the tools and tests hierarchy.
source "../prepare_tools.sh" "../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The GFFS tools and tests could not be automatically located.
  exit 1
fi

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

testCleaningPriorTestRun()
{
  echo taking steps to clean last test...
#if any.
}

testDoAThing()
{
  echo doing one thing
  assertEquals "doing that thing should work" 0 $?
}

testDoAnotherThing()
{
  echo doing another thing here
  assertEquals "doing that other thing should work" 0 $?

  false
  assertNotEquals "an explicit failure should be seen" 0 $?
}

oneTimeTearDown() {
  echo cleaning up after test now...
#if anything to do.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

