#!/bin/bash

# tests the grid container functionality out when using extended ACLS to protect exported
# hierarchies.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# this function takes a combined user and password, separated by a colon, and stores
# the user and password in separate global variables for immediate use.
function split_user_and_password()
{
  mylist=($(echo $1 | tr ':' ' '))
  GLOBAL_USER=${mylist[0]}
  GLOBAL_PASSWORD=${mylist[1]}
}

# displays the arguments expected for this command.
function print_usage()
{
  echo -e "\n$(basename $0): This test requires 5 major parameters."
  echo "The parameters are composed of 1 username, and 4 username:password pairs."
  echo -e "\nFor example:\n"
  echo "$(basename $0) Grantee Admin:AdminPassword Licensee:LicenseePassword \\"
  echo "    Feeble:FeeblePassword Flimsy:FlimsyPassword"
  echo -e "\nThese are required for the phases of the Extended ACLs test."
  echo "The unix user 'Grantee' is the primary user that is granted permissions via"
  echo "ACLs.  This is the user that runs the grid container."
  echo "There is also the currently logged in unix user, who will own the path to"
  echo "be exported, and who will grant the Grantee rights to the path.  This user"
  echo "must be different from Grantee for this test."
  echo "The grid user 'Admin' is one that can create a grid export."
  echo "The grid user 'Licensee' will be used to test the export permissions."
  echo "Licensee will be granted permission to use the export during the test."
  echo "A grid user 'Feeble' will not be given permissions on the export, and will"
  echo "be tested to ensure that he doesn't."
  echo "A unix user 'Flimsy', who is not Grantee or the current user, will not be"
  echo "given permissions on the filesystem path, and this will be checked during"
  echo "the test."
  echo "Note that grid users should be provided as full paths, such as /users/joe."
}

##############

# process the command line.

# get all the accounts and passwords loaded up.
Grantee="$1"; shift
temp="$1"; shift
split_user_and_password "$temp"
Admin="$GLOBAL_USER"
AdminPassword="$GLOBAL_PASSWORD"
temp="$1"; shift
split_user_and_password "$temp"
Licensee="$GLOBAL_USER"
LicenseePassword="$GLOBAL_PASSWORD"
temp="$1"; shift
split_user_and_password "$temp"
Feeble="$GLOBAL_USER"
FeeblePassword="$GLOBAL_PASSWORD"
temp="$1"; shift
split_user_and_password "$temp"
Flimsy="$GLOBAL_USER"
FlimsyPassword="$GLOBAL_PASSWORD"

# helpful debugging; don't leave turned on!
#echo "Got parameters: $Grantee $Admin=$AdminPassword $Licensee=$LicenseePassword $Feeble=$FeeblePassword $Flimsy=$FlimsyPassword"

if [ -z "$Grantee" \
    -o -z "$Admin" -o -z "$AdminPassword" \
    -o -z "$Licensee" -o -z "$LicenseePassword" \
    -o -z "$Feeble" -o -z "$FeeblePassword" \
    -o -z "$Flimsy" -o -z "$FlimsyPassword" ]; then
  echo -e "\nIncorrect number of parameters."
  print_usage
  exit 1
fi

error_string=""
if [ "$Grantee" == "$USER" ]; then error_string+="\tGrantee and current user ($Grantee)\n"; fi
if [ "$Licensee" == "$Feeble" ]; then error_string+="\tLicensee and Feeble user ($Licensee)\n"; fi
if [ "$Flimsy" == "$Grantee" ]; then error_string+="\tFlimsy and Grantee user ($Flimsy)\n"; fi
if [ "$Flimsy" == "$USER" ]; then error_string+="\tFlimsy and current user ($Flimsy)\n"; fi
if [ ! -z "$error_string" ]; then
  echo -e "\nThe users provided cannot be identical in these ways:"
  echo -e "$error_string"
  print_usage
  exit 1
fi

# check that the paths are what we expect.
if [ "$(basename $Feeble)" == "$Feeble" ]; then
  echo "Feeble user $Feeble is in wrong format; it needs to be specified as a full grid path"
  exit 1
fi
if [ "$(basename $Licensee)" == "$Licensee" ]; then
  echo "Licensee user $Licensee is in wrong format; it needs to be specified as a full grid path"
  exit 1
fi
if [ "$(basename $Admin)" == "$Admin" ]; then
  echo "Admin user $Admin is in wrong format; it needs to be specified as a full grid path"
  exit 1
fi

##############

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # we'll make our testing path for ACLs under here.
  LOCAL_PATH="$(mktemp -d $TEST_TEMP/acl_path.XXXXXX)"
  echo "Directory path for testing is: $LOCAL_PATH"
  chmod -R 777 "$LOCAL_PATH"
  assertEquals "Change $LOCAL_PATH to allow all permissions to all users" 0 $?
  # this path will be used for the grid version of our exported path.
#  EXPORTED_GRID_PATH="$RNSPATH/exporty"
  EXPORTED_GRID_PATH="$RNSPATH/exporty_$RANDOM"
#random version is only needed due to current bug in export --quit
}

testGoodUsersProvided()
{
  grid logout --all
  # login as the normal grid user they provided to test it works.
  grid login --username=$(basename $Licensee) --password=$LicenseePassword
  assertEquals "Licensee user $Licensee grid login should be successful" 0 $?

  grid logout --all
  # login as the feeble grid user they provided to test it works.
  grid login --username=$(basename $Feeble) --password=$FeeblePassword
  assertEquals "Feeble user $Feeble grid login should be successful" 0 $?

  grid logout --all
  # login as admin user to test that that works.
  grid login --username=$(basename $Admin) --password=$AdminPassword
  assertEquals "Admin user $Admin grid login should be successful" 0 $?
  # stay logged in as the admin for now; we will use that soon.

  # test logging in as unix flimsy.
  run_any_command expect $XSEDE_TEST_ROOT/library/ssh_expecter.tcl $Flimsy $FlimsyPassword localhost "echo hello venus"
  reval=$?
  # make sure we didn't experience a failure on the other side.
  grep YO_FAILURE $GRID_OUTPUT_FILE &>/dev/null
  if [ $? -eq 0 ]; then
    echo Detected failure running a command via ssh.
    ((retval++))
  fi
  assertEquals "Flimsy user $Flimsy unix login should be successful" 0 $retval
}

testPreparePathBasicRights()
{
  run_any_command bash $XSEDE_TEST_ROOT/library/set_acls.sh "$Flimsy" "$LOCAL_PATH" "000"
  assertEquals "Revoking all rights for Flimsy account $Flimsy should work" 0 $?
  run_any_command bash $XSEDE_TEST_ROOT/library/set_acls.sh "$Grantee" "$LOCAL_PATH" "rwx"
  assertEquals "Adding all rights for Flimsy account $Flimsy should work" 0 $?
}

testCheckFlimsyCannotAccess()
{
  run_any_command expect $XSEDE_TEST_ROOT/library/ssh_expecter.tcl $Flimsy $FlimsyPassword localhost "echo hello venus >$LOCAL_PATH/gerkins.txt" "mkdir $LOCAL_PATH/grommet"
  reval=$?
  # make sure we didn't experience a failure on the other side.
  grep YO_FAILURE $GRID_OUTPUT_FILE &>/dev/null
  if [ $? -eq 0 ]; then
    echo Detected failure running a command via ssh.
    ((retval++))
  fi
  assertEquals "user $Flimsy must try to run test commands on prohibited path" 0 $retval
  test -d $LOCAL_PATH/grommet
  assertNotEquals "path created by $Flimsy should not exist" 0 $?
  test -f $LOCAL_PATH/gerkins.txt
  assertNotEquals "file created by $Flimsy should not exist" 0 $?
}

testExportFromPath()
{
  # expecting to still be logged in as admin at this point...  i know this is a violation of
  # xunit standards, but it's ridiculous in bash to not expect the shunit tests to run in order.
  grid export --create $CONTAINERPATH/Services/LightWeightExportPortType local:$LOCAL_PATH grid:$EXPORTED_GRID_PATH
  assertEquals "path $EXPORTED_GRID_PATH should be created on export attempt" 0 $?

  grid chmod grid:$EXPORTED_GRID_PATH "000" $Feeble
  assertEquals "revoking permissions on exported path for Feeble user $Feeble" 0 $?
  grid chmod grid:$EXPORTED_GRID_PATH "000" $SUBMIT_GROUP
  assertEquals "revoking permissions on exported path for group $SUBMIT_GROUP" 0 $?
  grid chmod grid:$EXPORTED_GRID_PATH +rwx $Licensee
  assertEquals "adding permissions on exported path for Licensee user $Licensee" 0 $?
}

# tests that a local file can be copied from this machine up to a path on the grid,
# and then can be copied back again with no changes in the file.
# it takes the local filename and a grid path as its two arguments.
# if a third parameter is provided, it is taken as the "correct" answer for the
# exit value from the attempts to copy; if this is provided as 1, then failure is
# expected.
function checkGFFSSanity()
{
  local localfile="$1"; shift
  local path="$1"; shift
  local exit_value="$1"; shift
  # expect success by default, if they didn't tell us different.
  if [ -z "$exit_value" ]; then exit_value=0; fi

  local garbagefile=$(mktemp $TEST_TEMP/garbleman.XXXXXX)
  local intermediate="$path/grotty_turnips_$RANDOM.txt"

  check_function=assertEquals

  extra_phrase=""
  if [ $exit_value == 1 ]; then
    extra_phrase="not "
    check_function=assertNotEquals
  fi

  grid cp local:$localfile grid:$intermediate
  $check_function "user should ${extra_phrase}be able to write in directory" 0 $?

  grid cp grid:$intermediate local:$garbagefile
  $check_function "user should ${extra_phrase}be able to copy from directory" 0 $?

  if [ $exit_value == 0 ]; then
    # they expected that all to work, so let's keep going.
    diff $localfile $garbagefile &>/dev/null
    assertEquals "Copied file should remain same after going to grid and back" 0 $?

    grid rm grid:$intermediate
    assertEquals "Should be able to clean up grid copy of file" 0 $?
  fi

  \rm $garbagefile
}

testLicenseeCanAccess()
{
  grid logout --all
  # get back in as our 'normal' guy now, the one who should be able to mess with
  # things on the path.
  grid login --username=$(basename $Licensee) --password=$LicenseePassword
  assertEquals "Licensee user $Licensee grid login should still be successful" 0 $?

  # copy this script up as a test file.
  checkGFFSSanity "$0" "$EXPORTED_GRID_PATH"

  # try some directory stuff.
  grid mkdir grid:$EXPORTED_GRID_PATH/flambe
  assertEquals "Licensee should be able to create directories on export" 0 $?

  # test out the directory we just made.
  checkGFFSSanity "$0" "$EXPORTED_GRID_PATH/flambe"

  grid cd grid:$EXPORTED_GRID_PATH/flambe
  assertEquals "Licensee should be able to change to the directory we just made" 0 $?

  grid rm -r grid:$EXPORTED_GRID_PATH/flambe
  assertEquals "Should be able to cleanup directory we just made" 0 $?

  # set up a file that we can check as the feeble user.
  grid cp local:$XSEDE_TEST_ROOT/inputfile.txt grid:$EXPORTED_GRID_PATH
  assertEquals "Copying a file up to the folder should work" 0 $?
}

testFeebleIsBlocked()
{
  grid logout --all
  # login as the feeble grid user that we have set up for failure on the grid version
  # of the exported path.
  grid login --username=$(basename $Feeble) --password=$FeeblePassword
  assertEquals "Feeble user $Feeble grid login should be successful again" 0 $?

  expect_failure=1

  # copy this script up as a test file.
  checkGFFSSanity "$0" "$EXPORTED_GRID_PATH" $expect_failure

  # try some directory stuff.
  grid mkdir grid:$EXPORTED_GRID_PATH/smoozic
  assertNotEquals "Feeble user should not be able to create directories on export" 0 $?

  # test out the directory we just made.
  checkGFFSSanity "$0" "$EXPORTED_GRID_PATH/smoozic" $expect_failure

  grid cd grid:$EXPORTED_GRID_PATH
  grid ls
  assertNotEquals "Feeble user should not be able to change to exported directory" 0 $?

  # trash any existing file.
  \rm -f $TEST_TEMP/foobar.txt
  grid cp grid:$EXPORTED_GRID_PATH local:$TEST_TEMP/foobar.txt
  assertNotEquals "Copying a file down from the folder should not work" 0 $?

  # make sure there's still nothing there.
  test -f $TEST_TEMP/foobar.txt
  assertNotEquals "File should not have magically showed up" 0 $?
}

testCleaningUpExport()
{
  # unexport the path (as admin user).
  grid logout --all
  grid login --username=$(basename $Admin) --password=$AdminPassword
  assertEquals "Admin user $Admin grid login should still be successful" 0 $?

  grid export --quit grid:$EXPORTED_GRID_PATH
  assertEquals "Exported path should quit cleanly" 0 $?

#hmmm.....
#not quitting cleanly yet, although doesnt give an error.
#this seems to be a bug in export --quit, where the path is no longer visible,
#but some remnant of it causes the grid folder to still seem to be present when
#we try to use the same name.
#need to report bug.

}

oneTimeTearDown()
{
  # clean up the created path.  this should be utterly safe with export gone.
  \rm -rf "$LOCAL_PATH"
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

