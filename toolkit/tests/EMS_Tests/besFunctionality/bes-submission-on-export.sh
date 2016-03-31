#!/bin/bash

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
}

testDirectoryCleanups()
{
  # clean up our testing folder if it exists.
echo RNSPATH here is $RNSPATH
  grid ping --eatfaults $RNSPATH &>/dev/null
  if [ $? == 0 ]; then
    echo cleaning up existing testing folder: $RNSPATH
    grid rm -r $RNSPATH
    assertEquals "Removing rns path for testing of $RNSPATH" 0 $?
  fi

  # take out prior export.
  grid ping --eatfaults $FULL_EXPORT_PATH &>/dev/null
  if [ $? == 0 ]; then
    echo primary clean up of existing export location: $FULL_EXPORT_PATH
    # close any export there.
    silent_grid export --quit $FULL_EXPORT_PATH
    grid ping --eatfaults $FULL_EXPORT_PATH &>/dev/null
    if [ $? == 0 ]; then
echo export quit did not remove the export path.  is that normal?
      # now clean it up as a simple folder, since it's still present.
      silent_grid unlink $FULL_EXPORT_PATH &>/dev/null
      retval=$?
      assertEquals "primary removal of export location: $FULL_EXPORT_PATH" 0 $retval
      if [ $retval -ne 0 ]; then
        echo "The grid export quit attempt on $FULL_EXPORT_PATH failed although we"
        echo "believe the file is present.  This is not a good sign."
      fi
    fi
  fi
}

testCreateExport()
{
  # Create an export on the container from our test area.
  grid export --create $CONTAINERPATH/Services/LightWeightExportPortType local:$EXPORTPATH grid:$FULL_EXPORT_PATH
  assertEquals "Creating export on $CONTAINERPATH, local path $EXPORTPATH, at $FULL_EXPORT_PATH" 0 $?

  # clean up our testing folder if it exists.  we do this again because it's possible the
  # export wasn't available, but this folder already exists on that local path.
  grid ping --eatfaults $RNSPATH &>/dev/null
  if [ $? == 0 ]; then
    echo secondary clean up of existing testing folder: $RNSPATH
    grid rm -r $RNSPATH
    assertEquals "Second removal of rns test path: $RNSPATH" 0 $?
  fi

  # now that the export exists, the level above our testing directory should exist, so we
  # can create the test dir itself.
  grid mkdir -p "$RNSPATH"
  assertEquals "making subdirectory under export directory for staging" 0 $?
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

testCleaningUpFiles()
{
  grid rm "$RNSPATH/cat.out" "$RNSPATH/cat.err"
  assertEquals "Removing staging files" 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
  testDirectoryCleanups
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

