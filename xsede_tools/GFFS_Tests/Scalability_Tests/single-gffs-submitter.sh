#!/bin/bash

# Author: Vanamala Venkataswamy
# Author: Chris koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

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

OUR_MOUNT_POINT=$WORKDIR/mount-single-gffs-submitter-$user

##############

# allows deletion of files and directories.  will not attempt it if
# it does not see the item present.  this is NOT appropriate to use
# on a fuse-mounted directory!
function simple_delete()
{
  local rm_ret=0
  local i=
  for i in $*; do
#echo evaluating $i...
    if [ -f "$i" ]; then
echo removing file $i
      rm -f "$i" &>/dev/null
      rm_ret=$?
    elif [ -d "$i" ]; then
echo removing directory $i
      rm -rf "$i" &>/dev/null
      rm_ret=$?
    fi
    if [ $rm_ret -ne 0 ]; then
      echo "SAW ERROR DURING REMOVAL OF $i"
    fi
  done
}

##############

# become the user we were told to be.
put_on_hat "$user" "$password" "$idp_path"
if [ $? -ne 0 ]; then
  echo Failed to assume identity of user $user
  exit 2
fi

if ! fuse_supported; then
  exit 0
fi
# cleanup prior mounts.
fusermount -u $OUR_MOUNT_POINT &>/dev/null
if [ -d $OUR_MOUNT_POINT ]; then
  rmdir $OUR_MOUNT_POINT
  if [ $? -ne 0 ]; then
    echo "Failed to remove our soon-to-be mount point, which already existed: $OUR_MOUNT_POINT"
    exit 1
  fi
fi
# mount the FS on local FS
mkdir $OUR_MOUNT_POINT
if [ $? -ne 0 ]; then
  echo "Failed to create our mount point at: $OUR_MOUNT_POINT"
echo "here is the directory above that:"
ls $(dirname $OUR_MOUNT_POINT)
  exit 1
fi
fuse --mount local:$OUR_MOUNT_POINT
sleep 30  # give process time to get mounted.
checkMount=`mount`
if [[ "$checkMount" =~ .*$OUR_MOUNT_POINT.* ]]; then
  retval=0
else
  echo "Mount failed... Exiting out."
  exit 1
fi

# run the test a partially random number of times.
test_count=$(expr $RANDOM % 3 + 1)

echo Will run $test_count tests

for (( i=0; i < $test_count; i++ )); do

#hmmm: we've added random numbers to the path to avoid issues with removing a link
#      created during the build; currently links are not reference counted, and the
#      link's removal can lead to issues in the build.  plan to get past this is to
#      implement links with reference counts.  at that point, we could return to
#      a simpler, non-random path.
BUILD_TARGET=$OUR_MOUNT_POINT/$RNSPATH/${user}_builds/temp_$RANDOM$RANDOM$RANDOM

mkdir -p $BUILD_TARGET 
if [ $? -ne 0 ]; then
  echo "Failed to create the build target at: $BUILD_TARGET"
  exit 1
fi

  echo "Test round $(expr $i + 1) for '$user'."
  # we'll pick one or the other job type.
  fail_count=0
  if [ $(expr $RANDOM % 2) -gt 0 ]; then
    # create/delete gnu source
    jobtype="gzip Make"
    simple_delete $BUILD_TARGET/gzip-1.2.4.tar $BUILD_TARGET/gzip-1.2.4
    echo snoozing to allow cache to clear
    sync
    sleep $GFFS_CACHE_SNOOZE
##
#echo user directory after gzip removed and sleeping shows...
#ls $BUILD_TARGET
#if [ -d $BUILD_TARGET/gzip-1.2.4 ]; then
#  echo Saw gzip folder still, trying cat... 
#  cat $BUILD_TARGET/gzip-1.2.4/nt/Makefile.nt
#  echo "=======  above was cat, if worked."
#fi
##
    cp ./gzip-1.2.4.tar $BUILD_TARGET
    if [ $? -ne 0 ]; then 
      ((fail_count++))
      echo "Could not make initial gzip file copy to folder, bailing out."
      i=$(expr $test_count + 2)
    fi
    pushd $BUILD_TARGET
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    tar xvf gzip-1.2.4.tar
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    cd gzip-1.2.4
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    bash ./configure
    make prefix=./ install
    popd
  else
    # create/delete iozone source
    jobtype="iozone Make"
    simple_delete $BUILD_TARGET/iozone3_397.tar $BUILD_TARGET/iozone3_397
    echo snoozing to allow cache to clear
    sync
    sleep $GFFS_CACHE_SNOOZE
##
#echo user directory after iozone removed and sleeping shows...
#ls $BUILD_TARGET
#if [ -d $BUILD_TARGET/iozone3_397 ]; then
#  echo Saw gzip folder still, trying cat... 
#  cat $BUILD_TARGET/iozone3_397/src/current/Generate_Graphs
#  echo "=======  above was cat, if worked."
#fi
##
    cp ./iozone3_397.tar $BUILD_TARGET
    if [ $? -ne 0 ]; then 
      ((fail_count++))
      echo "Could not make initial iozone file copy to folder, bailing out."
      i=$(expr $test_count + 2)
    fi
    pushd $BUILD_TARGET
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    tar xvf iozone3_397.tar
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    cd iozone3_397/src/current/
    if [ $? -ne 0 ]; then ((fail_count++)); fi
    make clean
    make linux
    popd
  fi
  echo "$(date): performed job type $jobtype"
  if [ $fail_count -ne 0 ]; then
    echo "Failed running $jobtype for $user"
    ((TEST_FAIL_COUNT++))
  fi

##hmmm: disabled due to issues with links described above.
### clean up anything from the test run.
##rm -rf $BUILD_TARGET

done  # end of creating/deleting files

# last step is to unhook the fuse mount, so we can be done with this user completely.
if [ -d $OUR_MOUNT_POINT ]; then
  fusermount -u $OUR_MOUNT_POINT
  rmdir $OUR_MOUNT_POINT
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

