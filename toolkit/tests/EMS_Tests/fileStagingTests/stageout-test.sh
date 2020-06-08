#!/bin/bash

#Author: Vanamala Venkataswamy
#mods: Chris Koeritz, Charlie Houghton

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "Copying necessary file to Grid namespace:" $RNSPATH
  grid cp local:$PWD/hostname-sleep.sh grid:$RNSPATH
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testEarlyTerminate()
{
	submit="$(grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/early-terminate.jsdl)"
	assertEquals "Submitting single job that we intend to terminate in 30 seconds with file staging from GFFS using OGSA BYTE/IO protocol" 0 $?
	ticket="$(echo $submit | awk '{print $9}' | tr -d \" | tr -d .)"
	grep -i "Job Submitted" $GRID_OUTPUT_FILE
	echo "Waiting 30 seconds for job to start and produce partial output..."
	sleep 30
	grid qkill $QUEUE_PATH $ticket
	echo "Killed. Waiting 30 seconds for data to be staged-out..."
	sleep 30
	grid ls grid:$RNSPATH
	grid cat grid:$RNSPATH/hostname-sleep.out
	grid ls $RNSPATH
	grid cat $RNSPATH/hostname-sleep.out
	hostname_sleep_output="$(grid cat grid:$RNSPATH/hostname-sleep.out)"
	echo $hostname_sleep_output | grep -q Host-sleep
	assertEquals "Jobs that were terminated early should have (incomplete) files staged out." 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

