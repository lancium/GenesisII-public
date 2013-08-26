#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

user_count=10
  # we would like to run with this many demo users.

# note: even with just 2 users, this test causes a very quick failure, where the target file
#       seems to lose all access permissions.

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  cp -f ../Performance_Tests/*.tar .
  if [ ${#MULTI_USER_LIST[*]} -lt $user_count ]; then
    user_count=${#MULTI_USER_LIST[*]}
    echo "Backing down to only $user_count demo users, because the MULTI_USER_LIST does not have enough."
  fi
}

testMultipleUserLaunch()
{
  assertNotEquals "There need to be demo users defined in MULTI_USER_LIST for this test to operate." 0 $user_count
  # iterate across the demo user numbers.
  local i
  for (( i=0; $i < $user_count; i++)); do
    user=${MULTI_USER_LIST[ $i ]}
    password=${MULTI_PASSWORD_LIST[ $i ]}
#echo got user $user and passwd $password
    # launch the drone with the proper authentication info.
    log_file=$TEST_TEMP/crush_gffs_${i}_$(basename $user).log
    echo $(date): client $i will log to $log_file
    bash $WORKDIR/single-gffs-crusher.sh $(basename $user) $password $user &>$log_file &
  done
}

testGatherMultiUserResults()
{
  # snooze a bit before trying to acquire results.
  sleep 28

  # we don't care who exits first, as long as they all do.  we'll gather
  # the results and figure out how many failed.
  fail_count=0
  local i
  for (( i=0; $i < $user_count; i++)); do
    # wait for the i'th job.
    wait "%$(expr $i + 1)"
    if [ $? != 0 ]; then
      ((fail_count++))
      user=${MULTI_USER_LIST[ $i ]}
      echo "Saw a failure returned from drone for $user."
    fi
  done
  assertEquals "No asynchronous jobs for our concurrent users should fail" 0 $fail_count
}

oneTimeTestTearDown()
{
  rm -f iozone3_397.tar
  rm -f gzip-1.2.4.tar
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

