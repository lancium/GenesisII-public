#!/bin/bash

# per-user script for submitting just a few jobs.
#
# Author: Chris Koeritz

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# take the parms for which user to run as.
user="$1"; shift
password="$1"; shift
idp_path="$1"; shift
submit_flag="$1"; shift
index="$1"; shift

if [ -z "$user" -o -z "$password" -o -z "$idp_path" -o -z "$submit_flag" -o -z "$index" ]; then
  echo "This script takes five parameters: a user and password, an IDP path"
  echo "for authentication, a submit flag (which can be 'BES' for direct"
  echo "submission or anything else for queue submission), and an index that"
  echo "tells which user instance this is.  It will run jobs as the grid user."
  exit 1
fi

# first we sleep a random amount to ensure that the jobs aren't simply started
# at the same exact time.  they start close to each other though.
sleep $(expr $RANDOM % 3)

# we track how many non-fatal errors were encountered and use this to judge
# whether the test worked or not.  fatal errors exit immediately.
TEST_FAIL_COUNT=0

# become the user we were told to be.
put_on_hat "$user" "$password" "$idp_path"
if [ $? -ne 0 ]; then
  echo Failed to assume identity of user $user
  exit 2
fi

if [ "$submit_flag" == "BES" ]; then
  # acquire the BES resource that we'll test against.
  BES_CONTAINER="$(get_BES_resources)"
  # strip down to just the first item.
  BES_CONTAINER=${BES_CONTAINER[0]}
  echo BES_CONTAINER is $BES_CONTAINER
fi

# run the test a partially random number of times.
#test_count=$(expr $RANDOM % 32 + 24)
test_count=$(expr $RANDOM % 4 + 4)

for (( i=0; i < $test_count; i++ )); do
  echo "Test round $(expr $i + 1) for '$user'."
  # we'll pick one or the other job type.
  if [ $(expr $RANDOM % 2) -gt 0 ]; then
    # add in an ls job.
    jobtype="ls"
    if [ "$submit_flag" != "BES" ]; then
      grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/ls-single-job-$index.jsdl
    else
      grid run --name=ls-job-$RANDOM --jsdl=local:$GENERATED_JSDL_FOLDER/ls-single-job-$index.jsdl $BES_CONTAINER
    fi
  else
    # add a hostname job.
    jobtype="hostname"
    if [ "$submit_flag" != "BES" ]; then
      grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/hostname-stage-out-$index.jsdl
    else
      grid run --name=hostname-job-$RANDOM --jsdl=local:$GENERATED_JSDL_FOLDER/hostname-stage-out-$index.jsdl $BES_CONTAINER
    fi
  fi
  retval=$?
  echo $(date): performed job type $jobtype
  if [ $retval -ne 0 ]; then
    echo Failed submitting $jobtype job.
    ((TEST_FAIL_COUNT++))
  fi

  if [ $(expr $RANDOM % 100) -gt 75 ]; then
    # we decided to snooze for a bit before next submission.
    sleep $(expr $RANDOM % 3)
  fi

done  # end of job submissions and other activity.

sleep 10  # snooze to allow jobs to begin to percolate through.

echo "will wait for jobs $QUEUE_TRIES_ALLOWED times (at $QUEUE_SLEEP_DURATION seconds each)."

# now gather up results from that run.
wait_for_all_pending_jobs $QUEUE_PATH
if [ $? -ne 0 ]; then
  echo Failed while waiting for queue to clear after test runs.
  ((TEST_FAIL_COUNT++))
fi

# stop being the user.
take_off_hat
if [ $? -ne 0 ]; then
  echo Failed to relinquish identity of user $user
  exit 2
fi

# report our results.  we don't simply return the fail count, because
# we could potentially have more than 127 failures, which would swamp the
# return value.
if [ $TEST_FAIL_COUNT -ne 0 ]; then
  exit 1
else
  exit 0
fi

