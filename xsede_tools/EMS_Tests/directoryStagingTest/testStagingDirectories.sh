#!/bin/bash

# Tests the capability for staging directories in and out during job processing.

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

source "../../prepare_tools.sh" "../../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The xsede tool suite could not be automatically located.
  exit 1
fi

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  # clean-up any prior run.
  grid rm -rf $RNSPATH/job_scripts $RNSPATH/feisty_meow $RNSPATH/murphy
#?  rm -rf $WORKDIR/murphy
}

testUploadStagingContents()
{
  echo "Copying necessary files to Grid namespace"
  grid cp -r local:./job_scripts grid:$RNSPATH
  assertEquals "Copying job_scripts directory up" 0 $?
}

testSubmitJobWithDirectoryStaging()
{
  echo "starting submit... $(date)"

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/copy_hierarchy.jsdl
  assertEquals "Submitting copy hierarchy job to stage some directories" 0 $?

  echo "ending submit... $(date)"
}

testWaitForDirectoryStagingJob()
{
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "No directory staging jobs should be left" 0 $?
}

testResultingDirectoryStageOut()
{

#is this looking in the right place?  like in grid?

  echo listing out the build products of note...
  ls murphy/binaries/nechung*
  assertEquals "nechung binary did not get built as expected" 0 $?
  chmod 755 ./murphy/binaries/nechung
  # set the nechung database environment variable so the app can find the fortunes.
  export NECHUNG=/home/fred/feisty_meow/nucleus/applications/nechung/example.txt
  echo running the produced binary...
  ./murphy/binaries/nechung
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

