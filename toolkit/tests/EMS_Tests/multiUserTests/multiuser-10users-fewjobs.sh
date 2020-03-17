#!/bin/bash

# queue-based job submission test with lots of users but not very many jobs.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

SUBMIT_FLAG="$1"; shift
  # if the submit flag is BES, then the BES is submitted to directly.
  # any other value is interpreted as meaning that queue submission is fine.
if [ -z "$SUBMIT_FLAG" ]; then
  # if they don't provide any flag, we assume they're okay with queue submission.
  SUBMIT_FLAG="queue"
fi

user_count=10
  # we would like to run with this many demo users.

if [ ${#MULTI_USER_LIST[*]} -lt $user_count ]; then
  user_count=${#MULTI_USER_LIST[*]}
  echo "Backing down to only $user_count demo users, because the MULTI_USER_LIST does not have enough."
fi
if [ $user_count -le 0 ]; then
  echo "There need to be demo users defined in MULTI_USER_LIST for this test to operate; skipping test."
  exit 0
fi

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  replicate_jsdl_file hostname-stage-out 10
  replicate_jsdl_file ls-single-job 10

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi
}

testGetStagingFilesUploaded()
{
  echo "Copying necessary file to Grid namespace"
  grid cp local:$PWD/stagecoach-hostname.sh grid:$RNSPAT.H
  assertEquals "File upload should succeed" 0 $?
  grid chmod grid:$RNSPATH/stagecoach-hostname.sh +r --everyone
  assertEquals "chmod on staging file should work" 0 $?
}

testMultipleUserLaunch()
{
  # iterate across the demo user numbers.
  local i
  for (( i=0; $i < $user_count; i++)); do
    user=${MULTI_USER_LIST[ $i ]}
    password=${MULTI_PASSWORD_LIST[ $i ]}
    log_file=$TEST_TEMP/user_log_10few_${i}_$(basename $user).log
    # launch the drone with the proper authentication info.
    echo "$(date): client $i will log to $log_file"
    bash "$WORKDIR/one-drone-submitter.sh" "$(basename $user)" "$password" "$user" "$SUBMIT_FLAG" "$i" &>$log_file &
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
    user=${MULTI_USER_LIST[ $i ]}
    wait "%$(expr $i + 1)"
    if [ $? != 0 ]; then
      ((fail_count++))
      echo "Saw a failure returned from one of the drones, for '$user'."
    fi
  done
  assertEquals "No asynchronous jobs for our concurrent users should fail" 0 $fail_count
}

oneTimeTearDown()
{
  # clean up the replicated files.
  \rm -f *-[0-9].jsdl 
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

