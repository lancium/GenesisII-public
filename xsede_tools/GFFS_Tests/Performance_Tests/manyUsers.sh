#!/bin/bash
# this test attempts to create more than 100 users to assist testing our iterator changes.
# Author: Chris Koeritz

# the generated users will have this number as part of their name and password.
USER_NUMBER_START=1
USER_NUMBER_END=1200

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

testCreatingUsers()
{
  for ((unum=$USER_NUMBER_START; $unum <= $USER_NUMBER_END; unum++)); do
    echo user number $unum
    name="$USERS_LOC/some-user$unum"
    pass="orp$unum"
    bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$name" "$pass" "$SUBMIT_GROUP" "$HOMES_LOC" "$USERS_LOC"
    if [ $? -ne 0 ]; then
      echo "failed to create user $name"
    fi
  done
}

oneTimeTearDown() {
  echo tearing down test setup.
  echo now that users are created, try logging in as one of them.
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

