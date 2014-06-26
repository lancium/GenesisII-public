#!/bin/bash

# assorted useful ways of running the grid shell.
#
# Author: Chris Koeritz

##############

# echoes the right grid application to run for this platform.
function pick_grid_app()
{
  if [ "$OS" == "Windows_NT" ]; then
    dos_genii="$(echo "$GENII_INSTALL_DIR" | sed -e 's/\//\\/g')"
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

##############

# this is a helper function that hides the grid command's output unless an error
# is detected.  it does not do error checking, but it will return with the grid
# command's exit value.
function grid()
{
  grid_base \"$(pick_grid_app)\" "${@}"
}

# a specialized grid launcher that invokes the fuse tool and saves the output.
# this puts the fuse job into the background.
function fuse()
{
  local fuse_out="$(mktemp "$TEST_TEMP/grid_logs/out_fuse_$(date_string).XXXXXX")"
  logged_grid "$fuse_out" "$(pick_grid_app)" fuse $* &
}

# a function that behaves like the normal grid command, except that it times the
# processing that occurs from the command.
function timed_grid()
{
  grid_base $(\which time) -p -o "$GRID_TIMING_FILE" "$(pick_grid_app)" $*
}

# this bails out if an error occurs.
function grid_chk()
{
  echo "    g> $*"
  grid $*
  check_if_failed "'grid $*' exited with exit code $?"
}

# this function accepts a long stream of stuff from standard input which is passed
# into the grid command.  this allows using a here document as input to the command.
function multi_grid()
{
  grid_base \"$(pick_grid_app)\"
  local retval=$?
  if [ $retval -ne 0 ]; then
    # we have to exit here since the command is operating as a sub-shell with the new
    # input stream that the caller provides.
    echo "multi_grid failing with exit code $retval"
    exit $retval    
  fi
  return $retval
}

# this function takes the log file to use for the grid command, and it does not
# manage that file at all.
function logged_grid()
{
  local my_output="$1"; shift
  logged_command "$my_output" raw_grid "${@}"
  local retval=$?
  # make the external version of the log file available.  if we're multiplexing users,
  # this will be meaningless, which is why we used unique names above.
  \cp -f "$my_output" "$GRID_OUTPUT_FILE"
  return $retval
}

# the most basic grid client invocation function that people should use, in general. 
# this will only show the output file if there's a problem.
function grid_base()
{
  local my_output="$(mktemp $TEST_TEMP/grid_logs/out_grid_base_$(date_string).XXXXXX)"
  logged_grid $my_output "${@}"
  local retval=$?
  return $retval
}

# expects to be passed the grid OS path as the first argument.  produces output that
# has been filtered for gunk we don't want to see.
function raw_grid()
{
  # expects first parms to be the app/command to run.
  "${@}" 2>&1 | grep -v "Checking for updates\|Updates Disabled\|YourKit Java Profiler\|Current version is\|untoward for mooch"
  return ${PIPESTATUS[0]}
}

##############

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

##############

