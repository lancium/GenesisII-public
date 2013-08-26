#!/bin/bash

# Supports xsede test scripts with a few handy functions and many variables.
#
# Author: Chris Koeritz

##############

# commonly used variables get set up here...

# this variable points to the last output from a grid command.
export GRID_OUTPUT_FILE="$TEST_TEMP/grid_output.log"

export GRID_TIMING_FILE="$TEST_TEMP/grid_times.log"

##############

source $XSEDE_TEST_ROOT/library/process_configuration.sh

##############

# this should be called first, in oneTimeSetUp, in every test script that uses shunit.
function sanity_test_and_init()
{
  if [ -z "$WORKDIR" ]; then
    echo "The WORKDIR variable is not set.  This should be established by each test, near the top."
    exit 1
  fi
  # establish this for shunit so tests do not have to run in current directory.
  export SHUNIT_PARENT="$WORKDIR/$(basename $0)"
#echo set the shunit parent to: $SHUNIT_PARENT

  # show who we're logged in as.
  echo -e "\nCurrently logged in to the grid as:"
  grid whoami
  cat $GRID_OUTPUT_FILE
  echo

  if [ $(grep -ic "additional credentials" <$GRID_OUTPUT_FILE) -gt 0 ]; then
    # set up the RNSPATH folder, in case it doesn't already exist.
    grid mkdir --parents grid:$RNSPATH &>/dev/null
    grid chmod grid:$RNSPATH +rwx --everyone &>/dev/null
    if [ $? -ne 0 ]; then
      echo "Failed to modify permissions for $RNSPATH to make world writeable."
      exit 1
    fi
else
echo chose not to mess with the home folder, since we have no creds
  fi

  if ! fuse_supported; then 
    echo
    echo "======="
    echo "FUSE mounts not supported on this platform; FUSE test cases will be skipped."
    echo "======="
  fi
}

# now that we've defined the setup function, we want to use it to establish
# all our environment variables.  then the rest of the code in this script
# can feel free to run anything needed and the variables will all be valid.
define_and_export_variables
if [ $? -ne 0 ]; then
  echo Not all variables could be imported properly from the inputfile.txt.
  exit 1
fi

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

# now that we have the environment set up, we can pull in the JSDL generator
# and other scripts.
source $XSEDE_TEST_ROOT/library/generate_jsdl.sh
source $XSEDE_TEST_ROOT/library/user_management.sh
source $XSEDE_TEST_ROOT/library/job_processing.sh
source $XSEDE_TEST_ROOT/library/grid_management.sh
#source $XSEDE_TEST_ROOT/library/random_ids_manager.sh
source $XSEDE_TEST_ROOT/library/helper_methods.sh

# echoes the right grid application to run for this platform.
function pick_grid_app()
{
  if [ "$OS" == "Windows_NT" ]; then
    dos_genii=$(echo $GENII_INSTALL_DIR | sed -e 's/\//\\/g')
    if [ -f "$GENII_INSTALL_DIR/grid.exe" ]; then
      echo "$GENII_INSTALL_DIR/grid.exe"
    elif [ ! -z "$(uname -a | grep "^MING" )" ]; then
      echo "cmd //c $dos_genii\\grid.bat"
    else
      echo "cmd /c $dos_genii\\grid.bat"
    fi
  else
    # linux and others don't need a special version.	   
    echo "$GENII_INSTALL_DIR/grid"
  fi
}

# this is a helper function that hides the grid command's output unless an error
# is detected.
function grid()
{
  grid_base $(pick_grid_app) $*
}

# a specialized grid launcher that invokes the fuse tool and saves the output.
# this puts the fuse job into the background.
function fuse()
{
  if [ ! -d "$TEST_TEMP/grid_logs" ]; then mkdir -p "$TEST_TEMP/grid_logs"; fi
  local fuse_out="$(mktemp "$TEST_TEMP/grid_logs/fuse_output_$(date_string).XXXXXX")"
  logged_grid "$fuse_out" $(pick_grid_app) fuse $* &
}

# a function that behaves like the normal grid command, except that it times the
# processing that occurs from the command.
function timed_grid()
{
  grid_base $(\which time) -p -o "$GRID_TIMING_FILE" $(pick_grid_app) $*
}

# a helper method above and beyond the normal grid helper function;
# this bails out if an error occurs.
function grid_chk() {
  echo "    g> $*"
  grid $*
  if [ $? -ne 0 ]; then
    echo "*** Failed running: grid $*"
    exit 1
  fi
}

# this function accepts a long stream of stuff from standard input which is passed
# into the grid command.  this allows using a here document as input to the command.
function multi_grid()
{
  grid_base $(pick_grid_app)
}

# this function takes the log file to use for the grid command, and it does not
# manage that file at all.
function logged_grid()
{
  local my_output="$1"; shift
  raw_grid "$my_output" "$@"
  local retval=$?
  # make the external version available.  if we're multiplexing users,
  # this will be meaningless, which is why we used unique names above.
  \cp -f "$my_output" "$GRID_OUTPUT_FILE"
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
    cat "$my_output"
  fi
  return 1
}

# the most basic grid client invocation function that people should use, in general. 
# this will only show the output file if there's a problem.
function grid_base()
{
  if [ ! -d "$TEST_TEMP/grid_logs" ]; then mkdir -p "$TEST_TEMP/grid_logs"; fi
  local my_output="$(mktemp $TEST_TEMP/grid_logs/uniqgridout_$(date_string).XXXXXX)"
  logged_grid $my_output $@
  local retval=$?
  return $retval
}

# this method always produces output into the first argument, and takes the
# remaining arguments as a command to run.
function raw_grid()
{
  local my_output="$1"; shift
  # expects first parms to be the app/command to run.
  "$@" 2>&1 | grep -v "Checking for updates\|Updates Disabled\|YourKit Java Profiler\|Current version is\|untoward for mooch" &>"$my_output"
  return ${PIPESTATUS[0]}
}

# runs a command and checks on the result.  if the command failed, then the output from it is
# displayed and an error code is returned.  otherwise the output is discarded.

#hmmm: very similar to grid_base, but there are some specializations in grid_base we want to keep
#      there.
function run_and_check()
{
  if [ ! -d "$TEST_TEMP/grid_logs" ]; then mkdir -p "$TEST_TEMP/grid_logs"; fi
  local my_output="$(mktemp $TEST_TEMP/grid_logs/uniqgridout_$(date_string).XXXXXX)"
  # expects first parms to be the app/command to run.
  "$@" &>"$my_output"
#hmmm: above is only place the commands need to differ?  grid_base needs the output strained
#      before giving the output file back.
  local retval=$?
  # make the external version available.  if we're multiplexing users,
  # this will be meaningless, which is why we used unique names above.
  # we refer to the internal output file name below though, since we don't
  # want to be tricked by some concurrent access into showing the wrong file.
  \cp -f "$my_output" "$GRID_OUTPUT_FILE"
  if [ $retval == 0 ]; then
    # good so far, but check for more subtle ways of failing; if there is
    # an occurrence of our fail message in the output, that also indicates
    # the command did not succeed.
    grep "\[FAILURE\]" $my_output
    if [ $? != 0 ]; then
      # we did not want to see that phrase in the log, and we did not.
      return 0  # fine exit, can ignore log.
    fi
  fi
  # this was a failure, so we need to see the log.
  cat "$my_output"
  return 1
}

# calculates the bandwidth for a transfer.  this takes the elapsed time as
# the first parameter and the size transferred as second parameter.
calculateBandwidth()
{
  local real_time="$1"; shift
  local size="$1"; shift
  # drop down to kilobytes rather than bytes.
  size=$(echo $size / 1024 | $(\which bc) -l)

#echo "time=$real_time size=$size"

  local total_sec="$(echo "$real_time" | awk -Fm '{print $1}'| awk -F. '{print $1}' )"
  local bandwidth=""
  if [ $total_sec -ne 0 ]; then
    bandwidth="$(echo "scale=3; $size / $total_sec" | $(\which bc) -l)"
  else
    bandwidth="infinite"
  fi
  echo "$bandwidth"
}

# a wrapper for calculateBandwidth that prints out a nicer form of the
# bandwidth.  it requires the same parameters as calculateBandwidth.
showBandwidth()
{
  echo "  Bandwidth $(calculateBandwidth $*) kbps"
}


