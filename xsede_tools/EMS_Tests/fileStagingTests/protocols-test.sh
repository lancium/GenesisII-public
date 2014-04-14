#!/bin/bash

##Author: Vanamala Venkataswamy

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "Copying necessary file to Grid namespace"
  grid cp local:./cat.sh grid:$RNSPATH
  grid cp local:./hostname.sh grid:$RNSPATH
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testHTTP()
{
  for i in $available_resources; do
  	grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-http.jsdl $i
  	assertEquals "Submitting single cat job with file staging using HTTP protocol" 0 $?
  	grep -i "Job Submitted" $GRID_OUTPUT_FILE
  done
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "Pending http jobs should complete without errors." 0 $?
}

testFTP()
{
  for i in $available_resources; do
    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-ftp.jsdl $i
    assertEquals "Submitting single cat job with file staging using FTP protocol" 0 $?
    grep -i "Job Submitted" $GRID_OUTPUT_FILE
  done
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "Pending ftp jobs should complete without errors." 0 $?
}

testGFFS()
{
  for i in $available_resources; do
    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-gffs.jsdl $i
    assertEquals "Submitting single cat job with file staging from GFFS using OGSA BYTE/IO protocol" 0 $?
    grep -i "Job Submitted" $GRID_OUTPUT_FILE
  done
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "Pending gffs jobs should complete without errors." 0 $?
}

testSCP()
{
  for i in $available_resources; do
        grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-scp.jsdl $i
  	assertEquals "Submitting single cat job with file staging using SCP protocol" 0 $?
  	grep -i "Job Submitted" $GRID_OUTPUT_FILE
  done
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "Pending scp jobs should complete without errors." 0 $?
}

testSFTP()
{
  echo "BES type is: $BES_TYPE"  
  # we can't use these types of staging if the BES is a Unicore BES.
  if [ "$BES_TYPE" = "Genesis" ]; then
    for i in $available_resources; do
      grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-sftp.jsdl $i
      assertEquals "Submitting single cat job with file staging using the SFTP protocol" 0 $?
      grep -i "Job Submitted" $GRID_OUTPUT_FILE
    done
    wait_for_all_pending_jobs $QUEUE_PATH whack
    assertEquals "Pending sftp jobs should complete without errors." 0 $?
  else
    echo "Skipping test as this protocol is not supported by this BES"
  fi
}

testMailTo()
{
    for i in $available_resources; do
      grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-mailto.jsdl $i
      assertEquals "Submitting single cat job with file staging using the MAILTO protocol" 0 $?
      grep -i "Job Submitted" $GRID_OUTPUT_FILE
    done
    wait_for_all_pending_jobs $QUEUE_PATH whack
    assertEquals "Pending mailto jobs should complete without errors." 0 $?

}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

