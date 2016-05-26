#!/bin/bash

### NOTE: this test is not currently working.
### it needs some fixes / updates.

# Tests the capability for staging directories in and out during job processing.

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

source "../../../prepare_tools.sh" "../../../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The xsede tool suite could not be automatically located.
  exit 1
fi

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

  oneTimeTearDown
}

testUploadStagingContents()
{
  echo "Copying necessary files to Grid namespace"
  grid cp -r local:./job_scripts local:./vi-source grid:$RNSPATH
  assertEquals "Copying job_scripts and source directory up" 0 $?
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
  # get the output side version.
  grid cp -r "$RNSPATH/murphy" local:./murphy

  out1=$(mktemp $TEST_TEMP/infiles.XXXXXX)
  out2=$(mktemp $TEST_TEMP/outfiles.XXXXXX)

  pushd ./vi-source
  assertEquals "Changing to source directory for compare" 0 $?
  find . -type f >"${out1}.tmp"
  assertEquals "Running find on input directory to get file names" 0 $?
  sort "${out1}.tmp" > "$out1"
  rm "${out1}.tmp"
  popd

  pushd ./murphy
  assertEquals "Changing to source directory for compare" 0 $?
  find . -type f >"${out2}.tmp"
  assertEquals "Running find on output directory to get file names" 0 $?
  sort "${out2}.tmp" > "$out2"
  rm "${out2}.tmp"
  popd

  outpoof=$(comm -23 "$out1" "$out2")
  assertEquals "Comparing the files listed in both dirs with comm to see if any of the original files are missing" 0 $?

  fsize=${#outpoof}
  assertEquals "List of files missing from first set should be empty" 0 $fsize

}

oneTimeTearDown()
{
  echo cleaning up test area...
  # clean-up any prior run.
  silent_grid rm -r $RNSPATH/job_scripts $RNSPATH/vi-source $RNSPATH/murphy &>/dev/null
  rm -rf $WORKDIR/murphy
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

