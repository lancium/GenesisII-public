#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# the scratch dir below must agree with the configured scratch space.
SCRATCHDIR=/tmp/scratch

JOB_AREA=$RNSPATH/test-scratchfs

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  grid rm -rf $JOB_AREA &>/dev/null
  grid rm -f $RNSPATH/scratch.*

  if [ ! -d $SCRATCHDIR ]; then
    mkdir $SCRATCHDIR
  fi

  rm -f status.out
}

testMakingAsyncFolder()
{
  grid mkdir $JOB_AREA
  assertEquals "Making $JOB_AREA directory" 0 $?
  grid cp local:./scratch_job_script.sh $RNSPATH
  assertEquals "Copying script datafile to grid" 0 $?
  grid cp local:./from_scratch.txt $RNSPATH
  assertEquals "Copying from scratch datafile to grid" 0 $?
}

testBesResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testGetBesAttributes()
{
  for i in $available_resources; do
    silent_grid get-bes-attributes $i
    assertEquals "Get BES activities for factory $i" 0 $?
  done
}

testCreateActivity()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
    grid run --async-name=$JOB_AREA/$shortbes-scratcher --jsdl=local:$GENERATED_JSDL_FOLDER/simple-scratcher.jsdl $i
    assertEquals "Submitting single hostname job on $i" 0 $?
  done
}

testGetActivityAttributes()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
    silent_grid get-attributes $JOB_AREA/$shortbes-scratcher
    assertEquals "Checking activity attributes on $i" 0 $?
  done
}

testAwaitBesActivities()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
    poll_job_dirs_until_finished $JOB_AREA/$shortbes-scratcher
    if [ $? -ne 0 ]; then
      echo error seen from poll job dirs
      break
    fi
  done
  echo done polling jobs for completion

  echo output file has:
  grid cat $RNSPATH/scratch.out
  assertEquals "Testing output file presence" 0 $?
}

oneTimeTearDown()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
#hmmm: this is only necessary until we fix rm -r, which should carefully unlink resource forks rather than just giving up.
    grid unlink $JOB_AREA/$shortbes-scratcher 
  done
  grid rm -r $JOB_AREA 
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

