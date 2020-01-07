#!/bin/bash

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

# we will do this many rounds of the create + remove dir test.
MAX_ITERS_CREATE_REMOVE=6

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # clean up some things that might have been left from earlier test.
  grid rm -rf $RNSPATH/a $RNSPATH/a1 $RNSPATH/a2 $RNSPATH/a4 $RNSPATH/q $RNSPATH/garp $RNSPATH/1 $RNSPATH/foo &>/dev/null
  \rm -rf $TEST_TEMP/a1 $TEST_TEMP/foo $TEST_TEMP/crunchy $TEST_TEMP/a4.txt 
  \rm $TEST_TEMP/zorba &>/dev/null
  \rm -rf $TEST_TEMP/petunia &>/dev/null
}

#hmmm: at top just to see this run first for now..

testRecursiveMkdirAndRemove()
{
  silent_grid ping testdir --eatfaults
  if [ $? -eq 0 ]; then
    # directory already exists, so remove it before starting test.
    silent_grid rm -r testdir
    assertEquals "cleaning up existing testdir" 0 $?
  fi

  # we don't make all the dirs at once because the failure that caused an error
  # was doing the dir create and then a cd afterwards.  so we are mimicking the
  # same process here.
  local cmds_list="\n\
cd $RNSPATH\n\
mkdir testdir\n\
onerror failed to create new subdir\n\
cd testdir\n\
onerror failed to change to new subdir\n\
mkdir testdir\n\
onerror failed to create new subdir\n\
cd testdir\n\
onerror failed to change to new subdir\n\
mkdir testdir\n\
onerror failed to create new subdir\n\
cd testdir\n\
onerror failed to change to new subdir\n\
mkdir testdir\n\
onerror failed to create new subdir\n\
cd testdir\n\
onerror failed to change to new subdir\n\
pwd\n\
echo testing > testing.txt\n\
onerror failed to create new file in the subdir\n\
ls\n\
cd $RNSPATH\n\
rm -r testdir\n\
onerror failed to remove new subdirs\n\
"

#echo cmds list is:
#echo -e $cmds_list

  local iter
  for (( iter=0; $iter < $MAX_ITERS_CREATE_REMOVE; iter++ )); do
    multi_grid <<eof
echo doing first run of create and remove...
$(echo -e $cmds_list)
echo doing second run of create and remove...
$(echo -e $cmds_list)
echo doing third run of create and remove...
$(echo -e $cmds_list)
echo doing fourth run of create and remove...
$(echo -e $cmds_list)
eof
    assertEquals "multi-grid command with multiple create and remove attempts" 0 $?

echo here was output:
cat $GRID_OUTPUT_FILE

  done
}




testCopyEmptyDirectory()
{
  mkdir $TEST_TEMP/zorba
  cp $PWD/rnsBearTrap.sh $TEST_TEMP/zorba
  mkdir $TEST_TEMP/zorba/fruvil  # empty dir
  grid mkdir $RNSPATH/q
  assertEquals "making grid temporary dir should work" 0 $?
  # copy local to grid.
  grid cp -r local:$TEST_TEMP/zorba $RNSPATH/q
  assertEquals "copy local tree with empty dir to grid should work" 0 $?
  mkdir $TEST_TEMP/petunia
  # copy local to local.
  grid cp -r local:$TEST_TEMP/zorba local:$TEST_TEMP/petunia
  assertEquals "copy local tree with empty dir to local dir should work" 0 $?
  \rm -rf $TEST_TEMP/zorba $TEST_TEMP/petunia
  # check that nothing unexpected went wrong...
  \mkdir $TEST_TEMP/zorba
  assertEquals "making local dir again should work" 0 $?
  # copy grid to local.
  grid cp -r $RNSPATH/q local:$TEST_TEMP/zorba
  assertEquals "copy grid tree with empty dir to local path should work" 0 $?
  grid rm -r $RNSPATH/q  
  assertEquals "cleaning up grid temporary dir should work" 0 $?
  \rm -rf $TEST_TEMP/zorba 
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
  mkdir -p $TEST_TEMP/a1/b1/c1/d1
  grid cp -r local:$TEST_TEMP/a1 $RNSPATH/a2
  assertEquals "copying a1 to new hierarchy in grid should work" 0 $?
  grid ls $RNSPATH/a2/b1
  assertEquals "new hierarchy should start with b1 since dir was not pre-existing" 0 $?
  grid rm -r $RNSPATH/a2
  assertEquals "clean up a2 directory in grid" 0 $?
  grid mkdir $RNSPATH/a2
  assertEquals "pre-add a2 directory in grid" 0 $?
  grid cp -r local:$TEST_TEMP/a1 $RNSPATH/a2
  assertEquals "copying a1 to existing hierarchy in grid should work" 0 $?
  grid ls $RNSPATH/a2/a1
  assertEquals "new hierarchy should start with a1 since dir existed already" 0 $?
  grid rm -r $RNSPATH/a2
  assertEquals "clean up a2 directory in grid" 0 $?
  \rm -rf $TEST_TEMP/a1  # clean up local path.
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
  grid cp local:$PWD/$(basename $0) $RNSPATH/1/file
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
  local localpath="local:$TEST_TEMP/foo"
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
  grid cp local:$PWD/rnsBearTrap.sh $localpath/r/gumboot
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
  grid cp local:$PWD/rnsBearTrap.sh $RNSPATH/r/gumboot
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
  mkdir $TEST_TEMP/garp
  pushd $TEST_TEMP/garp &>/dev/null
  mkdir froon
  pushd froon &>/dev/null
  cp "$WORKDIR/rnsBearTrap.sh" razmo
  popd &>/dev/null; popd &>/dev/null

  grid cp -r local:$TEST_TEMP/garp $RNSPATH
  assertEquals "copy directory up to rns" 0 $?
  silent_grid cat $RNSPATH/garp/froon/razmo
  assertEquals "one copied file is accessible in rns" 0 $?

#apparently we do not pass this test yet; the file does get copied.
  # test copying a file onto itself.
#  grid cp $RNSPATH/garp/froon/razmo $RNSPATH/garp/froon/razmo
#  assertNotEquals "file cannot be copied onto itself" 0 $?

  # test copying a directory onto itself.
  grid cp $RNSPATH/garp/froon $RNSPATH/garp/froon
  assertNotEquals "directory cannot be copied onto itself" 0 $?

  mkdir $TEST_TEMP/junk
  grid cp -r $RNSPATH/garp local:$TEST_TEMP/junk
  assertEquals "copy back from rns to local" 0 $?
  silent_grid cat local:$TEST_TEMP/junk/garp/froon/razmo
  assertEquals "one copied file is accessible in local path" 0 $?

  \rm -rf $TEST_TEMP/junk
  assertEquals "cleanup junk hierarchy locally" 0 $?

  grid rm -r $RNSPATH/garp
  assertEquals "cleanup garp hierarchy in RNS" 0 $?

  \rm -rf $TEST_TEMP/garp
  assertEquals "cleanup garp hierarchy locally" 0 $?
}

testLocalDirWithCycles()
{
  if ! links_supported; then return 0; fi
  # avinash's specific case.
  pushd $TEST_TEMP &>/dev/null
  ln -s $TEST_TEMP $TEST_TEMP/zorba
  mkdir ./petunia
  grid cp -r local:$PWD/zorba local:$PWD/petunia
  assertNotEquals "copy simple local path with cycle should bounce" 0 $?
  \rm ./zorba &>/dev/null
  \rm -rf ./petunia &>/dev/null
  popd &>/dev/null

  # slightly more involved test.
  pushd $TEST_TEMP &>/dev/null
  mkdir ./foo
  mkdir ./foo/bork
  mkdir ./foo/moop
  mkdir ./foo/moop/freen
  mkdir ./crunchy
  ln -s ../../../foo ./foo/moop/freen/gackly
  # if either of these gets trapped in infinite loop, well that's a failure.
  # if they succeed and get what they can copied, not including links, then
  # that's a success currently.
  grid cp -r local:$PWD/foo local:$PWD/crunchy
  assertNotEquals "copy local path with cycle to local path should fail" 0 $?
  grid mkdir $RNSPATH/q
  assertEquals "make grid directory should work" 0 $?
  grid cp -r local:$PWD/foo $RNSPATH/q
  assertNotEquals "copy local path with cycle to grid path should fail" 0 $?
  grid rm -r $RNSPATH/q
  assertEquals "cleaning up grid path should work" 0 $?
  grid rm -r local:$PWD/crunchy
  assertEquals "cleaning up local crunchy path should work" 0 $?
  \rm -r ./foo
  popd &>/dev/null
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

oneTimeTearDown() {
  grid rm -rf $RNSPATH/a &>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

