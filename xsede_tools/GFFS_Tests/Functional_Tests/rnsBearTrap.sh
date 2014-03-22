#!/bin/bash

# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # clean up some things that might have been left from earlier test.
  grid rm -rf $RNSPATH/a $RNSPATH/a1 $RNSPATH/a2 $RNSPATH/a4 $RNSPATH/q $RNSPATH/garp $RNSPATH/1 $RNSPATH/foo &>/dev/null
  \rm -rf ./a1 ./foo ./crunchy ./a4.txt 
  \rm ./zorba &>/dev/null
  \rm -rf ./petunia &>/dev/null
}

testCopyEmptyDirectory()
{
  mkdir ./zorba
  cp ./README ./zorba
  mkdir ./zorba/fruvil  # empty dir
  grid mkdir $RNSPATH/q
  assertEquals "making grid temporary dir should work" 0 $?
  # copy local to grid.
  grid cp -r local:./zorba $RNSPATH/q
  assertEquals "copy local tree with empty dir to grid should work" 0 $?
  mkdir ./petunia
  # copy local to local.
  grid cp -r local:./zorba local:./petunia
  assertEquals "copy local tree with empty dir to local dir should work" 0 $?
  \rm -rf ./zorba ./petunia
  # check that nothing unexpected went wrong...
  \mkdir ./zorba
  assertEquals "making local dir again should work" 0 $?
  # copy grid to local.
  grid cp -r $RNSPATH/q local:./zorba
  assertEquals "copy grid tree with empty dir to local path should work" 0 $?
  grid rm -r $RNSPATH/q  
  assertEquals "cleaning up grid temporary dir should work" 0 $?
  \rm -rf ./zorba 
}

testProperHandlingGridPathType()
{
  grid cp a4.txt $RNSPATH
  assertNotEquals "Copying non-existent file should fail" 0 $?
  grid ls $RNSPATH/a4.txt
  assertNotEquals "No file should have showed up in grid" 0 $?
  grid mkdir $RNSPATH/foo
  assertEquals "Making new directory should succeed" 0 $?
  grid cp local:$0 $RNSPATH/foo/a4.txt
  assertEquals "Copying this file to grid should work" 0 $?
  grid cd $RNSPATH
  assertEquals "Changing grid cwd to test area" 0 $?
  grid cp foo/a4.txt $RNSPATH
  assertEquals "Copying existent file should work" 0 $?
  grid ls $RNSPATH/a4.txt
  assertEquals "File should show up in grid" 0 $?
  grid cd $RNSPATH/foo
  assertEquals "Changing grid cwd to path/foo" 0 $?
  grid cp a4.txt $RNSPATH
  assertEquals "Copying existent file from current dir should work" 0 $?
  grid ls $RNSPATH/grid:a4.txt
  assertNotEquals "Should not have created bizarre form of name" 0 $?
  grid cd $RNSPATH
  assertEquals "Changing grid cwd back to test area" 0 $?
  grid rm -r $RNSPATH/foo $RNSPATH/a4.txt
  assertEquals "Cleaning up files and dirs should be okay" 0 $?
}

testCopyingSimpleHierarchies()
{
  mkdir -p a1/b1/c1/d1
  grid cp -r local:./a1 $RNSPATH/a2
  assertEquals "copying a1 to new hierarchy in grid should work" 0 $?
  grid ls $RNSPATH/a2/b1
  assertEquals "new hierarchy should start with b1 since dir was not pre-existing" 0 $?
  grid rm -r $RNSPATH/a2
  assertEquals "clean up a2 directory in grid" 0 $?
  grid mkdir $RNSPATH/a2
  assertEquals "pre-add a2 directory in grid" 0 $?
  grid cp -r local:./a1 $RNSPATH/a2
  assertEquals "copying a1 to existing hierarchy in grid should work" 0 $?
  grid ls $RNSPATH/a2/a1
  assertEquals "new hierarchy should start with a1 since dir existed already" 0 $?
  grid rm -r $RNSPATH/a2
  assertEquals "clean up a2 directory in grid" 0 $?
  \rm -rf a1  # clean up local path.
}

testSalsFirstScenario()
{
  grid mkdir $RNSPATH/1
  assertEquals "making first directory should work" 0 $?
  grid mkdir $RNSPATH/1/2
  assertEquals "making second directory should work" 0 $?
  grid rm -r $RNSPATH/1
  assertEquals "removing first directory should work" 0 $?
  grid mkdir $RNSPATH/1
  assertEquals "making first directory again should work" 0 $?
  grid mkdir $RNSPATH/1/2
  assertEquals "making second directory again should work" 0 $?
  grid rm -r $RNSPATH/1
  assertEquals "tossing out first directory should work" 0 $?
}

testSalsSecondScenario()
{
  grid mkdir $RNSPATH/1
  assertEquals "making the directory should work" 0 $?
#  grid echo x \> $RNSPATH/1/file
#hmmm: above has issues on windows platform, so we create the file by copying in.
  grid cp local:./$(basename $0) $RNSPATH/1/file
  assertEquals "creating a simple file in the directory should work" 0 $?
  grid ls $RNSPATH/1/file
  assertEquals "simple file should exist after creation" 0 $?
  grid rm $RNSPATH/1
  assertNotEquals "removing the non-empty directory should not succeed" 0 $?
  grid rm -r $RNSPATH/1
  assertEquals "recursively removing the directory should succeed" 0 $?
  grid ls $RNSPATH/1
  assertNotEquals "directory should not exist after removal" 0 $?
}

testSalsThirdScenario()
{
  grid mkdir $RNSPATH/1
  assertEquals "making the directory should work" 0 $?
  grid cd $RNSPATH/1
  assertEquals "changing to the new directory should work" 0 $?
  grid rm $RNSPATH/1
  assertEquals "removing that new directory should work" 0 $?
  grid cd $RNSPATH
  assertEquals "changing to test directory should work" 0 $?
  grid mkdir $RNSPATH/1
  assertEquals "making the same directory again should work" 0 $?
  grid rm $RNSPATH/1
  assertEquals "tossing the directory again should still succeed" 0 $?
}

testSimpleLocalRemoval()
{
  local localpath="local:./foo"
  grid mkdir $localpath
  assertEquals "make top-level local directory" 0 $?
  grid rm $localpath
  assertEquals "drop top-level local directory" 0 $?
  grid mkdir $localpath
  assertEquals "make top-level local directory again" 0 $?
  grid mkdir $localpath/q
  assertEquals "make one local directory" 0 $?
  grid rm $localpath/q
  assertEquals "remove same local directory" 0 $?
  grid mkdir $localpath/r
  assertEquals "make one local directory" 0 $?
  grid cp local:./README $localpath/r/gumboot
  assertEquals "copy file into that local directory" 0 $?
  grid rm $localpath/r
  assertNotEquals "should not remove local directory with contents" 0 $?
  grid rm -r $localpath/r
  assertEquals "should recursively remove local directory with contents" 0 $?
  grid rm $localpath
  assertEquals "should remove local directory without contents" 0 $?
}

testSimpleGridRemoval()
{
  grid mkdir $RNSPATH/q
  assertEquals "make one rns directory" 0 $?
  grid rm $RNSPATH/q
  assertEquals "remove same rns directory" 0 $?
  grid mkdir $RNSPATH/r
  assertEquals "make one rns directory" 0 $?
  grid cp local:./README $RNSPATH/r/gumboot
  assertEquals "copy file into that rns directory" 0 $?
  grid rm $RNSPATH/r
  assertNotEquals "should not remove rns directory with contents" 0 $?
  grid rm -r $RNSPATH/r
  assertEquals "should recursively remove rns directory with contents" 0 $?
}

# want a test simple copy local also.

testSimpleCopyToGrid()
{
  # make a simple hierarchy to copy.
  mkdir garp
  pushd garp &>/dev/null
  mkdir froon
  pushd froon &>/dev/null
  cp ../../README razmo
  popd &>/dev/null; popd &>/dev/null

  grid cp -r local:./garp $RNSPATH
  assertEquals "copy directory up to rns" 0 $?
  grid cat $RNSPATH/garp/froon/razmo
  assertEquals "one copied file is accessible in rns" 0 $?

  mkdir junk
  grid cp -r $RNSPATH/garp local:./junk
  assertEquals "copy back from rns to local" 0 $?
  grid cat local:./junk/garp/froon/razmo
  assertEquals "one copied file is accessible in local path" 0 $?

  \rm -rf junk
  assertEquals "cleanup junk hierarchy locally" 0 $?

  grid rm -r $RNSPATH/garp
  assertEquals "cleanup garp hierarchy in RNS" 0 $?

  \rm -rf garp
  assertEquals "cleanup garp hierarchy locally" 0 $?
}

testLocalDirWithCycles()
{
  if ! links_supported; then return 0; fi
  # avinash's specific case.
  ln -s . ./zorba
  mkdir ./petunia
  grid cp -r local:./zorba local:./petunia
  assertNotEquals "copy simple local path with cycle should bounce" 0 $?
  \rm ./zorba &>/dev/null
  \rm -rf ./petunia &>/dev/null

  # slightly more involved test.
  mkdir ./foo
  mkdir ./foo/bork
  mkdir ./foo/moop
  mkdir ./foo/moop/freen
  mkdir ./crunchy
  ln -s ../../../foo ./foo/moop/freen/gackly
  # if either of these gets trapped in infinite loop, well that's a failure.
  # if they succeed and get what they can copied, not including links, then
  # that's a success currently.
  grid cp -r local:./foo local:./crunchy
  assertNotEquals "copy local path with cycle to local path should fail" 0 $?
  grid mkdir $RNSPATH/q
  assertEquals "make grid directory should work" 0 $?
  grid cp -r local:./foo $RNSPATH/q
  assertNotEquals "copy local path with cycle to grid path should fail" 0 $?
  grid rm -r $RNSPATH/q
  assertEquals "cleaning up grid path should work" 0 $?
  grid rm -r local:./crunchy
  assertEquals "cleaning up local crunchy path should work" 0 $?
  \rm -r ./foo
}

testRecursiveRnsDeleteWithLinks()
{
  grid mkdir $RNSPATH/a
  assertEquals "make top-level directory" 0 $?
  grid cd $RNSPATH/a
  assertEquals "cd to top-level directory" 0 $?
  grid mkdir b
  assertEquals "make one sub-directory" 0 $?
  grid cd b
  assertEquals "cd to sub-directory" 0 $?
  grid ln $RNSPATH/a c 
  assertEquals "link top-level directory to cause cycle" 0 $?
  grid cd $RNSPATH
  assertEquals "cd back to above the new directories" 0 $?
  grid rm -r $RNSPATH/a
  assertEquals "recursive removal should work" 0 $?
  grid ls $RNSPATH/a
  retval=$?
  assertNotEquals "directory should really be gone" 0 $retval
  if [ $retval -eq 0 ]; then
    # we only enter this on a success of the above ls (which means our test
    # actually failed to delete the directory).
    # so we authorize extreme sanction against this directory now.
    grid unlink $RNSPATH/a
    assertNotEquals "failed removal--should be able to unlink top level a" 0 $retval
  fi
}

# need a recursive delete test with links for local dirs!
# must implement support for checking for links though.

oneTimeTearDown() {
  grid rm -rf $RNSPATH/a &>/dev/null
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

