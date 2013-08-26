#!/bin/bash

# Author: Chris koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

OUR_TARGET_FILE=$RNSPATH/crush_target.txt

# take the parms for which user to run as.
user="$1"; shift
password="$1"; shift
idp_path="$1"; shift

if [ -z "$user" -o -z "$password" -o -z "$idp_path" ]; then
  echo "This script takes three parameters: a user name, a password, and an"
  echo "IDP path.  It will run jobs as that grid user."
  exit 3
fi

# first we sleep a random amount to ensure that the jobs aren't simply started
# at the same exact time.  they start close to each other though.
sleep $(expr $RANDOM % 3)

# we track how many non-fatal errors were encountered and use this to judge
# whether the test worked or not.  fatal errors exit immediately.
TEST_FAIL_COUNT=0

# we currently build in some sleeps to await file system consistency.
# this is required to be greater than 45 seconds due to current cached path issue. --cak
#GFFS_CACHE_SNOOZE=60
#GFFS_CACHE_SNOOZE=30
GFFS_CACHE_SNOOZE=0

##############

# become the user we were told to be.
put_on_hat "$user" "$password" "$idp_path"
if [ $? -ne 0 ]; then
  echo Failed to assume identity of user $user
  exit 2
fi

# run the test a partially random number of times.
test_count=$(expr $RANDOM % 6 + 6)

echo Will run $test_count tests

for (( i=0; i < $test_count; i++ )); do
  echo "Test round $(expr $i + 1) for '$user'."
  # we'll pick one or the other job type.
  fail_count=0

  action_case=$(expr $RANDOM % 100)
  if [ $action_case -lt 95 ]; then
    # 'normal' case where we append to the file.
#hmmm: so far there does not seem to be a way to append to a file from "the outside".
#      trying to see if same problem reproduces with a simple copy instead.
    echo "copying a file up onto the target."
    grid cp local:./$(basename $0) $OUR_TARGET_FILE
    if [ $? -ne 0 ]; then ((fail_count++)); fi
  else
    # oddity case where we just delete the file.
#    echo "removing the target file (may complain if non-existent)."
    echo "*not* removing the target file (may complain if non-existent)."
#temp
#    grid rm -f $OUR_TARGET_FILE
  fi

  if [ $fail_count -ne 0 ]; then
    echo "Failed running $jobtype for $user"
    ((TEST_FAIL_COUNT++))
  fi
done  # end of creating/deleting files

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

