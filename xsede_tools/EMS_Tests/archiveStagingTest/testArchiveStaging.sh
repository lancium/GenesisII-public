#!/bin/bash

# Tests the handling of archives as stage in and stage out data.

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
}

testUploadStagingContentsForBES()
{
  # invoke our own cleanup function before copying files up.
  oneTimeTearDown
  # copy the staging files up.
  uploadStagingContents
}

testFindBESName()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testRunJobWithStagedArchivesToBES()
{
  echo "starting submit... $(date)"

  # we just use the first BES for this test.
  BES_NAME=${available_resources[0]}

  echo "will run job on BES called: $BES_NAME"

  grid run --jsdl=local:$GENERATED_JSDL_FOLDER/gorp-returns.jsdl $BES_NAME
  assertEquals "Submitting single 'cat' job to cat a file on $BES_NAME" 0 $?

  echo "ending submit... $(date)"

  # there is no waiting needed for synchronous BES job.  we can move to next test.
}

testResultingStageOutFromBES()
{
  verifyResultingStageOut
}

testUploadStagingContentsForBES()
{
  # invoke our own cleanup function before copying files up.
  oneTimeTearDown
  # copy the staging files up.
  uploadStagingContents
}

testSubmitJobWithStagedArchivesToQueue()
{
  echo "starting submit... $(date)"

  grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/gorp-returns.jsdl
  assertEquals "Submitting archive staging job" 0 $?

  echo "ending submit... $(date)"
}

testWaitForDirectoryStagingJob()
{
  wait_for_all_pending_jobs $QUEUE_PATH whack
  assertEquals "No archive staging jobs should be left" 0 $?
}

testResultingStageOutFromQueue()
{
  verifyResultingStageOut
}

oneTimeTearDown()
{
  echo "cleaning up any test data files."

  cleanLocalTestFiles

  # clean-up any prior run's grid files.
  silent_grid rm $RNSPATH/gorp.zip $RNSPATH/snarfle.zip $RNSPATH/snarfle-from-gorp.sh &>/dev/null
}

##############

# helpful methods used above.

uploadStagingContents()
{
  echo "Copying necessary files to Grid namespace"
  grid cp local:gorp.zip grid:$RNSPATH
  assertEquals "Copying archive file up to sandbox" 0 $?
  grid cp local:snarfle-from-gorp.sh grid:$RNSPATH
  assertEquals "Copying script file up to sandbox" 0 $?
}

cleanLocalTestFiles()
{
  # clean up any local files and dirs we may have created.
  rm -rf snarfle.zip gorp_guts snarfle_guts
}

verifyResultingStageOut()
{
  grid cp $RNSPATH/snarfle.zip local:snarfle.zip
  assertEquals "Copying staged out archive file locally" 0 $?
  mkdir snarfle_guts gorp_guts
  pushd snarfle_guts &>/dev/null
  unzip ../snarfle.zip
  assertEquals "Unzipping staged out archive file locally" 0 $?
  popd &>/dev/null
  pushd gorp_guts &>/dev/null
  unzip ../gorp.zip
  popd &>/dev/null
  assertEquals "Unzipping original stage-in archive file locally" 0 $?
  diff -r gorp_guts snarfle_guts
  assertEquals "Testing archive for identical contents to original" 0 $?
  # toss out the testing junk.
  cleanLocalTestFiles
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

