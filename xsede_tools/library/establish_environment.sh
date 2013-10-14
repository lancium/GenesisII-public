#!/bin/bash

# Supports xsede test scripts with a few handy functions and many variables.
#
# Author: Chris Koeritz

##############

# commonly used environment variables...

# this variable points to the last output from a grid command.
export GRID_OUTPUT_FILE="$TEST_TEMP/grid_output.log"

export GRID_TIMING_FILE="$TEST_TEMP/grid_times.log"

##############

# the really basic helper functions...

# prints an error message (from parameters) and exits if the previous command failed.
function check_if_failed()
{
  if [ $? -ne 0 ]; then
    echo Step failed: $*
    exit 1
  fi
}

# takes a first parameter that is the name for a combined error and output log,
# and then runs all the other parameters as a command.
function logged_command()
{
#echo logcmd pwd: $(\pwd)
#echo logcmd args: "$@"
  local my_output="$1"; shift
  eval "$@" >>"$my_output" 2>&1
  local retval=$?
  if [ $retval == 0 ]; then
    # good so far, but check for more subtle ways of failing; if there is
    # an occurrence of our fail message in the output, that also indicates
    # the command did not succeed.
    grep "\[FAILURE\]" $my_output
    # we do not want to see that phrase in the log.
    if [ $? != 0 ]; then
      return 0  # fine exit, can ignore log.
    fi
  fi
  if [[ ! "$my_output" =~ .*fuse_output.* ]]; then
    # this was a failure, so we need to see the log.
    # fuse errors currently don't count since they are multifarious.
    cat "$my_output"
  fi
  return 1
}

# runs an arbitrary command.  if the command fails, then the output from it is
# displayed and an error code is returned.  otherwise the output is discarded.
function run_any_command()
{
  local my_output="$(mktemp $TEST_TEMP/grid_logs/out_run_any_cmd_$(date_string).XXXXXX)"
  logged_command "$my_output" "$@"
  local retval=$?
  # make the external version of the log file available.  if we're multiplexing users,
  # this will be meaningless, which is why we used unique names above.
  \cp -f "$my_output" "$GRID_OUTPUT_FILE"
  return $retval
}

##############

# makes sure that the RNSPATH is established before tests run.
function create_work_area()
{
  echo Checking work area in $RNSPATH
  grid whoami
  if [ $(grep -ic "additional credentials" <$GRID_OUTPUT_FILE) -gt 0 ]; then
    # set up the RNSPATH folder, in case it doesn't already exist.
    grid mkdir --parents grid:$RNSPATH &>/dev/null
    grid chmod grid:$RNSPATH +rwx $USERPATH
    check_if_failed Could not give $USERPATH permission to the work area $RNSPATH
  else
    echo Failed to find any credentials for running the regression tests.
    echo Please use login or xsedeLogin to authenticate as a grid identity.
    exit 1
  fi
}

# a helper method above and beyond the normal grid helper function;
# this should be called first, in oneTimeSetUp, in every test script that uses shunit.
function sanity_test_and_init()
{
  if [ -z "$WORKDIR" ]; then
    echo "The WORKDIR variable is not set.  This should be established by each test, near the top."
    exit 1
  fi
  # establish this for shunit so tests do not have to run in current directory.
  export SHUNIT_PARENT="$WORKDIR/$(basename $0)"

  create_work_area

  # show who we're logged in as.
  echo -e "\nCurrently logged in to the grid as:"
  grid whoami
  cat $GRID_OUTPUT_FILE
  echo

  if ! fuse_supported; then 
    echo
    echo "======="
    echo "FUSE mounts not supported on this platform; FUSE test cases will be skipped."
    echo "======="
  fi
}

##############

# read the inputfile.txt and generate environment variables for all the entries.
source $XSEDE_TEST_ROOT/library/process_configuration.sh
define_and_export_variables
check_if_failed Not all variables could be imported properly from the inputfile.txt.

##############

# prepare our logging area.
if [ ! -d "$TEST_TEMP/grid_logs" ]; then mkdir -p "$TEST_TEMP/grid_logs"; fi
if [ ! -d "$TEST_TEMP/job_processing" ]; then mkdir -p "$TEST_TEMP/job_processing"; fi

##############

# announce status if in debugging mode.

if [ ! -z "$DEBUGGING" -a -z "$SHOWED_SETTINGS_ALREADY" ]; then
  echo +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  echo Grid install located at $GENII_INSTALL_DIR
  echo User state directory at $GENII_USER_DIR
  echo Main container will be $CONTAINERPATH
  echo User path is $USERPATH and group is $SUBMIT_GROUP
  echo RNS testing path is $RNSPATH
  echo +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
fi
# try to not blast out the above block of info again during this run.
export SHOWED_SETTINGS_ALREADY=true

##############

# now that we have the environment set up, we can pull in all the functions
# for working on the grid.
source $XSEDE_TEST_ROOT/library/helper_methods.sh
source $XSEDE_TEST_ROOT/library/runner_functions.sh
source $XSEDE_TEST_ROOT/library/generate_jsdl.sh
source $XSEDE_TEST_ROOT/library/user_management.sh
source $XSEDE_TEST_ROOT/library/job_processing.sh
source $XSEDE_TEST_ROOT/library/grid_management.sh
source $XSEDE_TEST_ROOT/library/security_management.sh
#source $XSEDE_TEST_ROOT/library/random_ids_manager.sh

##############

