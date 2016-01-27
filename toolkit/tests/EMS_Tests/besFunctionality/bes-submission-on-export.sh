#!/bin/bash

# Author: Vanamala Venkataswamy
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  export FULL_EXPORT_PATH="$RNSPATH/export-local"

  # this test sneaks in and changes the rns path to be the export path, since
  # we want to stage out to that path.  this allows the jsdl generator to
  # continue just working on the RNSPATH.
  export RNSPATH="$FULL_EXPORT_PATH/toads"

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure."
    exit 1
  fi

  # take out prior export.
  grid ls $FULL_EXPORT_PATH &>/dev/null
  if [ $? == 0 ]; then
    grid export --quit $FULL_EXPORT_PATH
    if [ $? -ne 0 ]; then
      echo "The grid unlink attempt on $FULL_EXPORT_PATH failed although we"
      echo "believe the file is present.  This is not a good sign."
    fi
    # now clean it up regardless, if it's still there.
    grid unlink $FULL_EXPORT_PATH &>/dev/null
  fi

}

testCreateExport()
{
  # Create an export on the container from our test area.
  grid export --create $CONTAINERPATH/Services/LightWeightExportPortType local:$EXPORTPATH grid:$FULL_EXPORT_PATH
  assertEquals "Creating export on $CONTAINERPATH, local path $EXPORTPATH, at $FULL_EXPORT_PATH" 0 $?
  cat $GRID_OUTPUT_FILE

  grid mkdir -p "$FULL_EXPORT_PATH/toads"
  assertEquals "making subdirectory under export directory for staging"
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testRunningSynchronousJobs()
{
  for i in $available_resources; do
    echo "Submitting jobs on resource $(basename $i)..."
    grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-stdin.jsdl $i
    assertEquals "Submitting single 'cat' job to cat a file on $i" 0 $?

    grid cp "$RNSPATH/cat.out" local:"$TEST_TEMP/cat.out"
    assertEquals "Copying stage out file after job completion" 0 $?
    echo "Done with resource $(basename $i)."
  done
}

testCleaningUp()
{
  grid rm "$RNSPATH/cat.out" "$RNSPATH/cat.err"
  assertEquals "Removing staging files" 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

