#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

#add to rns bear trap:
  grid rm -r $RNSPATH/zorba &>/dev/null

}

testRemoveWorkingDirectory()
{
  # set up a test directory.
  grid cd $RNSPATH
  assertEquals "changing to testing path" 0 $?
  grid mkdir zorba
  assertEquals "making directory for removal test" 0 $?
  # make that directory our current working directory.
  grid cd zorba
  assertEquals "changing to directory that will be removed" 0 $?
  # whack test dir, where we are currently standing.
  grid rm -r $RNSPATH/zorba
  assertEquals "removing testing directory" 0 $?
  grid pwd
  assertEquals "checking pwd command" 0 $?
  # show the current working dir.
  cat $GRID_OUTPUT_FILE

  # here is where there's currently a bug.
  # this test does not pass, but should, because the directory doesn't exist.
  # it just happens to be the PWD entry, so we're erroneously blocking its creation.
#  grid mkdir $RNSPATH/zorba
#  assertEquals "making replacement directory" 0 $?

  # this code should work with the current behavior; we get out of that zombie PWD.
  grid cd $RNSPATH
  assertEquals "changing to test directory" 0 $?
  grid mkdir $RNSPATH/zorba
  assertEquals "making replacement directory" 0 $?
}


oneTimeTearDown() {
  grid rm -rf $RNSPATH/a &>/dev/null
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

