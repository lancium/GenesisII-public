#!/bin/bash

# simple test of revised run_any_command function to make sure it's still working.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  echo Setting up test.
#  sanity_test_and_init  # make sure test environment is good.
}

testGoodUsersProvided()
{
  run_any_command find "$XSEDE_TEST_ROOT/library" -type f
  assertEquals "run_any should succeed on simple find" 0 $?

  run_any_command bork-bork-bork
  assertNotEquals "run_any should fail on bad command" 0 $?

  run_any_command find /this/path/should/not/exist/at/least/for/this -type f
  assertNotEquals "run_any should fail on bad path for find" 0 $?
}

oneTimeTearDown()
{
  echo Tearing down test.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

