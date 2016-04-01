#!/bin/bash

# assorted useful ways of running the grid shell.
#
# Author: Chris Koeritz

##############

# echoes the right grid application to run for this platform.  this will use
# the fastgrid tool if possible.
#hmmm: currently not possible since kills fuse?  need to try...
function pick_grid_app()
{
  if [ "$OS" == "Windows_NT" ]; then
    dos_genii="$(echo "$GENII_INSTALL_DIR" | sed -e 's/\//\\/g')"
    if [ -f "$GENII_BINARY_DIR/grid.exe" ]; then
      echo "$GENII_BINARY_DIR/grid.exe"
    elif [ ! -z "$(uname -a | grep "^MING" )" ]; then
      echo "cmd //c $dos_genii\\bin\\grid.bat"
    else
      echo "cmd /c $dos_genii\\bin\\grid.bat"
    fi
  else
    # linux and mac don't need to grope about for the file quite as badly.
    echo "$GENII_BINARY_DIR/grid"
    # and they can now take advantage of the fastgrid script.
#    echo "$GENII_BINARY_DIR/fastgrid"
  fi
}

# echoes the proper "normal" grid application to run for this platform.
#hmmm: could eliminate one version if fast grid supported streaming or we build a work around.
function pick_unaccelerated_grid_app()
{
  if [ "$OS" == "Windows_NT" ]; then
    dos_genii="$(echo "$GENII_INSTALL_DIR" | sed -e 's/\//\\/g')"
    if [ -f "$GENII_BINARY_DIR/grid.exe" ]; then
      echo "$GENII_BINARY_DIR/grid.exe"
    elif [ ! -z "$(uname -a | grep "^MING" )" ]; then
      echo "cmd //c $dos_genii\\bin\\grid.bat"
    else
      echo "cmd /c $dos_genii\\bin\\grid.bat"
    fi
  else
    # linux and mac don't need to grope for the file as badly.
    echo "$GENII_BINARY_DIR/grid"
  fi
}

##############

# this is a helper function that hides the grid command's output unless an error
# is detected.  it does not do error checking, but it will return with the grid
# command's exit value.
function silent_grid()
{
  grid_base \"$(pick_grid_app)\" "${@}"
}

# a specialized grid launcher that invokes the fuse tool and saves the output.
# this puts the fuse job into the background.
function fuse()
{
  local fuse_out="$(mktemp "$TEST_TEMP/grid_logs/out_fuse_$(date_string).XXXXXX")"
  logged_grid "$fuse_out" "$(pick_unaccelerated_grid_app)" fuse $* &
  local retval=$?
#  echo "[$(readable_date_string)]"
  return $retval
}

# a function that behaves like the normal grid command, except that it times the
# processing that occurs from the command.
function timed_grid()
{
  echo "[started timer $(readable_date_string)]"
  # set a prefix for the date printout that happens after the command runs.
  date_stamp_prefix="stopped timer "
  grid_base $(\which time) -p -o "$GRID_TIMING_FILE" "$(pick_grid_app)" $*
  local retval=$?
#  echo "[$(readable_date_string)]"
  return $retval
}

# similar to timed_grid, but for arbitrary commands, and fits in with the timing
# calculators.
function timed_command()
{
  echo "[started timer $(readable_date_string)]"
  $(\which time) -p -o "$GRID_TIMING_FILE" $*
  local retval=$?
  echo "[stopped timer $(readable_date_string)]"
  return $retval
}

# this bails out if an error occurs.
function grid_chk()
{
  echo "[grid] $*" | sed -e 's/password=[^ ]* /password=XXXX /g'
  silent_grid $*
  check_if_failed "'grid ${1}...' exited with exit code $?"
#  echo "[$(readable_date_string)]"
}

function noisy_grid()
{
  echo "[grid] $*" | sed -e 's/password=[^ ]* /password=XXXX /g'
  silent_grid $*
  local retval=$?
  # show the output from the silent_grid command above, regardless of success.
  cat "$GRID_OUTPUT_FILE"
#  echo "[$(readable_date_string)]"
  return $retval
}

# default grid command is an alias for the noisy_grid method.
function grid()
{
  noisy_grid $*
}

# this function accepts a long stream of stuff from standard input which is passed
# into the grid command.  this allows using a here document as input to the command.
function multi_grid()
{
  grid_base \"$(pick_unaccelerated_grid_app)\"
  local retval=$?
  if [ $retval -ne 0 ]; then
    # we have to exit here since the command is operating as a sub-shell with the new
    # input stream that the caller provides.
    echo "multi_grid failing with exit code $retval"
#    echo "[$(readable_date_string)]"
    exit $retval    
  fi
#  echo "[$(readable_date_string)]"
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
  # then add the logging results to our huge mongo log of all actions.
  echo >> "$CONGLOMERATED_GRID_OUTPUT"
  echo "$(readable_date_string) log from: $my_output" >> "$CONGLOMERATED_GRID_OUTPUT"
  echo "=======" >> "$CONGLOMERATED_GRID_OUTPUT"
  cat "$my_output" >> "$CONGLOMERATED_GRID_OUTPUT"
  echo "=======" >> "$CONGLOMERATED_GRID_OUTPUT"
  # and now remove the tiny individual log file so we don't litter.
  \rm -f "$my_output"
  echo "[${date_stamp_prefix}$(readable_date_string)]"
  # we always reset the prefix after using it.
  unset date_stamp_prefix
  return $retval
}

# the most basic grid client invocation function that people should use, in general. 
# this will only show the output file if there's a problem.
function grid_base()
{
  local my_output="$(mktemp $TEST_TEMP/grid_logs/out_grid_base_$(date_string).XXXXXX)"
  logged_grid $my_output "${@}"
  local retval=$?
#  if [ $retval -ne 0 ]; then
#    # print a timestamp if there was an error.
#    echo "[$(readable_date_string)]"
#  fi
  return $retval
}

# expects to be passed the grid OS path as the first argument.  produces output that
# has been filtered for gunk we don't want to see.
function raw_grid()
{
  # expects first parms to be the app/command to run.
  "${@}" 2>&1 | grep -v "Checking for updates\|^\[.*\]$\|Updates Disabled\|YourKit Java Profiler\|Current version is\|untoward for mooch"
  return ${PIPESTATUS[0]}
}

##############

# uses the grid timing file to determine how long the last activity took in
# seconds and then prints out the value.
calculateTimeTaken()
{
  head -n 1 $GRID_TIMING_FILE | awk '{print $2}'
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
  if [[ "$total_sec" =~ .*exited.* ]]; then
    echo "FAILURE: Test run failed in some way; setting total seconds to very large number."
    total_sec="99999999"
  fi
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

# connects to a host as a particular user and executes a command there.
function run_command_remotely()
{
  if [ $# -lt 3 ]; then
    echo This function connects to a remote host to run a command.  It requires
    echo at least three parameters: the host to connect to, the user name on
    echo that host which supports passwordless logins, and the command to run.
    echo The command to run is the third through Nth parameters.
  fi
  host="$1"; shift
  username="$1"; shift
  # run our expecter to feed commands in, and the last one needs to be exit so we
  # return to the original host.
  OUTFILE="$(mktemp $TMP/ssh_run.XXXXXX)"
  expect $GFFS_TOOLKIT_ROOT/library/ssh_expecter.tcl "$username" "" "$host" "${@}" >"$OUTFILE"
  reval=$?
  # make sure we didn't experience a failure on the other side.
  grep "YO_FAILURE" $OUTFILE &>/dev/null
  if [ $? -eq 0 ]; then
    echo Detected failure running command via ssh.
    ((retval++))
  fi

#debugging
echo ========== output from command ============
cat "$OUTFILE"
echo ===========================================

  rm "$OUTFILE"
  return $retval
}

#testing
#run_command_remotely serene fred "ls /"

##############

