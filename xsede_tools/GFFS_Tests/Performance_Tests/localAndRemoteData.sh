#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../../prepare_tests.sh ../../prepare_tests.sh 
fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  if [ ! -f random5KB.dat ]; then
    echo "creating 5KB file..."
    dd if=/dev/urandom of=random5KB.dat bs=1 count=5120
  fi
 
  if [ ! -f random5MB.dat ]; then
    echo "creating 5MB file..."
    dd if=/dev/urandom of=random5MB.dat bs=1048576 count=5
  fi

  HUGE_TEST_FILE=./random5GB.dat
  if [ ! -f "$HUGE_TEST_FILE" ]; then
    # to speed up continuous integration builds, we will re-use the following
    # file if it's already available.  only defect is if it isn't actually 5
    # gigs.  but this should not be an issue for people doing normal testing.
    local PREMADE_FILE=$TMP/premade_5gig.dat
    if [ -f $PREMADE_FILE ]; then
      echo "using a pre-constructed file for 5gigs example: $PREMADE_FILE" 
      HUGE_TEST_FILE="$PREMADE_FILE"
    else
      echo "creating 5GB file, this may take a few minutes..."
      dd if=/dev/urandom of=$HUGE_TEST_FILE bs=1048576 count=5120
    fi
  fi

  # whack test directories just in case.
  grid rm -rf $RNSPATH/new-dir-local &>/dev/null
  grid rm -rf $RNSPATH/new-dir-remote &>/dev/null
  grid rm -rf $RNSPATH/new-dir &>/dev/null

  sync

  export TIMEFORMAT='%R'
}

testMakingContainerDirectories()
{
  echo "Creating test directories."

  grid mkdir --rns-service=$LOCAL_CONTAINER/Services/EnhancedRNSPortType $RNSPATH/new-dir-local
  assertEquals "making directory $RNSPATH/new-dir-local using RNS from $LOCAL_CONTAINER should work" 0 $?
  grid mkdir --rns-service=$REMOTE_CONTAINER/Services/EnhancedRNSPortType $RNSPATH/new-dir-remote
  assertEquals "making directory $RNSPATH/new-dir-remote using RNS from $REMOTE_CONTAINER should work" 0 $?
  grid mkdir --rns-service=$REMOTE_CONTAINER/Services/EnhancedRNSPortType $RNSPATH/new-dir
  assertEquals "making directory $RNSPATH/new-dir using RNS from $REMOTE_CONTAINER should work" 0 $?
}

testLocalClientLocalData()
{
  #small file
  timed_grid cp local:./random5KB.dat $RNSPATH/new-dir-local
  assertEquals "Timing - Transferring 5KB local file to $RNSPATH/new-dir-local on local container $LOCAL_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE | awk '{print $2}')
  echo "Time taken to tranfer 5KB file : $real_time s"
  actual_size=$(\ls -l ./random5KB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #medium file
  timed_grid cp local:./random5MB.dat $RNSPATH/new-dir-local
  assertEquals "Timing - Transferring 5MB local file to $RNSPATH/new-dir-local on local container $LOCAL_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5MB file : $real_time s"
  actual_size=$(\ls -l ./random5MB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #large file
  timed_grid cp local:$HUGE_TEST_FILE $RNSPATH/new-dir-local/random5GB.dat 
  assertEquals "Timing - Transferring 5GB local file to $RNSPATH/new-dir-local on local container $LOCAL_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5GB file : $real_time s"
  actual_size=$(\ls -l $HUGE_TEST_FILE | awk '{print $5}')
  showBandwidth "$real_time" $actual_size
}

testLocalClientRemoteData()
{
  #small file
  timed_grid cp local:./random5KB.dat $RNSPATH/new-dir-remote
  assertEquals "Timing - Transferring 5KB local file to $RNSPATH/new-dir-remote on remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5KB file : $real_time s"
  actual_size=$(\ls -l ./random5KB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #medium sized file
  timed_grid cp local:./random5MB.dat $RNSPATH/new-dir-remote
  assertEquals "Timing - Transferring 5MB local file to $RNSPATH/new-dir-remote on remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5MB file : $real_time s"
  actual_size=$(\ls -l ./random5MB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #large file
  timed_grid cp local:$HUGE_TEST_FILE $RNSPATH/new-dir-remote/random5GB.dat
  assertEquals "Timing - Transferring 5GB local file to $RNSPATH/new-dir-remote on remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5GB file : $real_time s"
  actual_size=$(\ls -l $HUGE_TEST_FILE | awk '{print $5}')
  showBandwidth "$real_time" $actual_size
}

testRemoteSrcAndDest()
{
  #small file
  timed_grid cp $RNSPATH/new-dir-local/random5KB.dat $RNSPATH/new-dir
  echo $time
  assertEquals "Timing - Transferring 5KB local file to $RNSPATH/new-dir from local container $LOCAL_CONTAINER to remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5KB file : $real_time s"
  actual_size=$(\ls -l ./random5KB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #medium file
  timed_grid cp $RNSPATH/new-dir-local/random5MB.dat $RNSPATH/new-dir
  assertEquals "Timing - Transferring 5MB local file to $RNSPATH/new-dir from local container $LOCAL_CONTAINER to remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5MB file : $real_time s"
  actual_size=$(\ls -l ./random5MB.dat | awk '{print $5}')
  showBandwidth "$real_time" $actual_size

  #large file
  timed_grid cp $RNSPATH/new-dir-local/random5GB.dat $RNSPATH/new-dir
  assertEquals "Timing - Transferring 5GB local file to $RNSPATH/new-dir from local container $LOCAL_CONTAINER to remote container $REMOTE_CONTAINER" 0 $?
  sync
  real_time=$(head -n 1 $GRID_TIMING_FILE |awk '{print $2}')
  echo "Time taken to tranfer 5GB file : $real_time s"
  actual_size=$(\ls -l $HUGE_TEST_FILE | awk '{print $5}')
  showBandwidth "$real_time" $actual_size
}

testCleanupDirectories()
{
  grid rm -r $kludge $RNSPATH/new-dir-local
  assertEquals "Cleanup of new-dir-local should work" 0 $?
  grid rm -r $kludge $RNSPATH/new-dir-remote
  assertEquals "Cleanup of new-dir-remote should work" 0 $?
  grid rm -r $kludge $RNSPATH/new-dir
  assertEquals "Cleanup of new-dir should work" 0 $?
}

oneTimeTearDown()
{
  rm random5KB.dat random5MB.dat
  # only delete the 5gig file if we made a new one.
  if [ -f random5GB.dat ]; then rm random5GB.dat; fi
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

