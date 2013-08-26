#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

MOUNT_POINT="$WORKDIR/mount-gffsGridCommands"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # remove any previous mount point.
  fusermount -u $MOUNT_POINT &>/dev/null
  rmdir $MOUNT_POINT &>/dev/null
  # clean up directories the xml scripts want to create.
  grid rm -rf ${RNSPATH}/TestDir\* ${RNSPATH}/test-rns $RNSPATH/crumpet.txt &>/dev/null
  # remove the export if still present from interrupted prior test.
  grid export --quit $RNSPATH/exportTest &>/dev/null
  # toss local file.
  \rm -f ./crumpet.txt
}

testNoFoolingAroundWithMount()
{
  # create a new mount point.
  mkdir $MOUNT_POINT
  assertEquals "making mount directory at $MOUNT_POINT" 0 $?
}

testRNSPathExists()
{
  grid ping $RNSPATH 
  retval=$?
  assertEquals "Pinging RNS Path" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because RNS is not pingable."
    exit 1
  fi
}

testTestDirDoesntExist()
{
  grid ls ${RNSPATH}/TestDir
  assertNotEquals "TestDir should be clean from prior step" 0 $?
}

testSimplestCopy()
{
  \rm -f crumpet.txt
  assertEquals "clean local test file mirror" 0 $retval
  grid cp local:$0 $RNSPATH/crumpet.txt
  assertEquals "copy local test file to grid" 0 $retval
  grid cp $RNSPATH/crumpet.txt local:.
  assertEquals "copy test file from grid back to local" 0 $retval
  diff $0 crumpet.txt &>/dev/null
  assertEquals "check that local and grid file are same" 0 $retval
  grid rm $RNSPATH/crumpet.txt
  assertEquals "cleaned up grid file okay" 0 $retval
  \rm crumpet.txt
  assertEquals "cleaned up local file okay" 0 $retval
}

testRNSFunctioning() {
  grid script local:./rnsTest.xml $RNSPATH 
  assertEquals "Testing RNS" 0 $?
}

testRandomByteIO() {
  grid script local:./randomByteIO.xml $RNSPATH
  assertEquals "Testing RandomByteIO" 0 $?
}

testBasicGridCLI() {
  if ! fuse_supported; then return 0; fi
  OUTFILE=$(mktemp $TEST_TEMP/gridCmdLine.XXXXXX)
  echo "Verbose run log is recorded in $OUTFILE"
  grid script local:./gridCmdLine.xml $RNSPATH $MOUNT_POINT $CONTAINERPATH $EXPORTPATH
  local retval=$?
  assertEquals "Testing basic grid command line" 0 $retval
  # make the actual copy for inspection later.
  cp -f $GRID_OUTPUT_FILE $OUTFILE
}

oneTimeTearDown() {
  grid rm -rf $RNSPATH/test-rns &>/dev/null
  fusermount -u $MOUNT_POINT &>/dev/null
  rmdir $MOUNT_POINT 
  \rm -f test1.txt
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

