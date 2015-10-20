#!/bin/bash

# Test: check for soft errors from logging out and back in.
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

# this needs to be relative to where the test will actually reside; the ../../../../../etc
# should get to the top of the tools and tests hierarchy.
source "../../prepare_tools.sh" "../../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The GFFS tools and tests could not be automatically located.
  exit 1
fi

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# this is the default log file for clients.
CLIENT_LOG_FILE=$HOME/.GenesisII/grid-client.log

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # move the current log file so we can look for any issues.
#  mv $CLIENT_LOG_FILE $TEST_TEMP/temporary_client_log.hold
}

testCleaningPriorTestRun()
{
  echo taking steps to clean last test...
}

testErrorStateBefore()
{
  echo Error state before running test:
  check_logs_for_errors
}

testLogoutAndLogin()
{
  echo getting root privileges on grid...
  get_root_privileges

  echo trying multi command sequence that has caused problems before...

multi_grid <<eof
  echo doing some listing first...
  ls
  cd
  ls
  ls /
  ls *
  echo now logging out...
  logout --all 
  echo and logging back in...
  login --username=$(basename $USERPATH) --password=$NORMAL_ACCOUNT_PASSWD
  onerror failed to login as expected user
eof
  assertEquals "logging out and then back in" 0 $?
  cat $GRID_OUTPUT_FILE

}

testErrorStateAfter()
{
  echo Error state after running test:
  check_logs_for_errors

echo THIS SHOULD HAVE NO NEW ERRORS! develop a check for that!
}

oneTimeTearDown() {
  echo cleaning up after test now...

  # put the log file back.
#  mv $TEST_TEMP/temporary_client_log.hold $CLIENT_LOG_FILE 
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"


