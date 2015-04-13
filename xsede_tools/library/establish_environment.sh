#!/bin/bash

# Supports xsede test scripts with a few handy functions and many variables.
#
# Author: Chris Koeritz

##############

# pull in the really basic helper functions...
source "$XSEDE_TEST_ROOT/library/helper_methods.sh"

##############

# makes sure that the RNSPATH is established before tests run.
function create_work_area()
{
  echo -e "\nCurrently logged in to the grid as:"
  grid whoami
  cat $GRID_OUTPUT_FILE
  echo
  if [ $(grep -ic "additional credentials" <$GRID_OUTPUT_FILE) -gt 0 ]; then
    # set up the RNSPATH folder, in case it doesn't already exist.
    echo Checking work area in $RNSPATH
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
  export SHUNIT_PARENT="$WORKDIR/$(basename "$0")"

  # show who we're logged in as.
#  echo -e "\nCurrently logged in to the grid as:"
#  grid whoami
#  cat $GRID_OUTPUT_FILE
#  echo

  create_work_area

  if ! fuse_supported; then 
    echo
    echo "======="
    echo "FUSE mounts not supported on this platform; FUSE test cases will be skipped."
    echo "======="
  fi
}

##############

# this is the main source of parameters for the tests.
export XSEDE_TOOLS_CONFIG_FILE
if [ -z "$XSEDE_TOOLS_CONFIG_FILE" ]; then
  # old default is used first if we don't have a setting for the config file.
  XSEDE_TOOLS_CONFIG_FILE="$XSEDE_TEST_ROOT/inputfile.txt"
fi
if [ ! -f "$XSEDE_TOOLS_CONFIG_FILE" ]; then
  # newer more rational config file name which also matches well with our
  # environment variable...  but this file must exist or we're stumped.
  XSEDE_TOOLS_CONFIG_FILE="$XSEDE_TEST_ROOT/xsede_tools.cfg"
fi
if [ ! -f "$XSEDE_TOOLS_CONFIG_FILE" -a -z "$BADNESS" ]; then
  echo "----"
  echo "This script requires that you prepare a customized file in:"
  echo "    $XSEDE_TOOLS_CONFIG_FILE"
  echo "with the details of your grid installation.  There are some example"
  echo "config files in the folder '$XSEDE_TEST_ROOT/examples'."
  BADNESS=true
fi

##############

# read the config file and generate environment variables for all the entries.
source "$XSEDE_TEST_ROOT/library/process_configuration.sh"
define_and_export_variables
check_if_failed "Not all variables could be imported properly from the configuration file '$XSEDE_TOOLS_CONFIG_FILE'"

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

# load in our secret passwords, if we have the appropriate file.

# we want these to always be visible to sub-scripts.
export ADMIN_ACCOUNT_PASSWD NORMAL_ACCOUNT_PASSWD 

# load passwords if they've seen fit to give us any.
if [ -f "$HOME/.secrets/grid_passwords.txt" ]; then 
  source "$HOME/.secrets/grid_passwords.txt"
fi

# set defaults for any passwords we didn't find a value for.
if [ -z "$ADMIN_ACCOUNT_PASSWD" ]; then ADMIN_ACCOUNT_PASSWD="admin"; fi
if [ -z "$NORMAL_ACCOUNT_PASSWD" ]; then NORMAL_ACCOUNT_PASSWD="FOOP"; fi

##############

# now that we have the environment set up, we can pull in all the functions
# for working on the grid.
source "$XSEDE_TEST_ROOT/library/runner_functions.sh"
source "$XSEDE_TEST_ROOT/library/generate_jsdl.sh"
source "$XSEDE_TEST_ROOT/library/user_management.sh"
source "$XSEDE_TEST_ROOT/library/job_processing.sh"
source "$XSEDE_TEST_ROOT/library/grid_management.sh"
source "$XSEDE_TEST_ROOT/library/security_management.sh"
#source $XSEDE_TEST_ROOT/library/random_ids_manager.sh

##############

