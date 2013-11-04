#!/bin/bash
#
# Runs a command remotely on a BES given a pre-existing ssh credential
# that allows password-less login.  The handiest thing here is the ability
# to start and stop the container remotely, if it's on a known BES host.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
export POSSIBLY_UNBUILT=true
source $XSEDE_TEST_ROOT/library/establish_environment.sh

##hmmm: this is pretty general; move to the library.
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
  expect $XSEDE_TEST_ROOT/library/ssh_expecter.tcl "$username" "" "$host" "${@}" >"$OUTFILE"
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

# the list of hosts where we have Genesis BES installs.
BES_HOST_LIST=( \
  camillus.cs.virginia.edu \
  sierra.astro.virginia.edu \
  fir.itc.virginia.edu \
  149.165.146.134 \
  s79r.idp.sdsc.futuregrid.org \
  129.114.32.10 \
  login1.rockhopper.uits.iu.edu \
  login2.uc.futuregrid.org \
)

# this is the list of users for the above hosts.  this must stay in lock-step
# with the host list.
BES_USER_LIST=( \
  xcgbes \
  xcguser \
  xcgrun \
  xcguser \
  xcguser \
  xcguser \
  uva_xcg \
  koeritz \
)
# may want to make the name on the futuregrid host a special variable.

# the install list must be in lock-step with the host list.
BES_INSTALL_FOLDERS=( \
  /localtmp/xcgbes/xcg3-container/GenesisII \
  /home/staff/xcguser/XCG3/GenesisII \
  /state/partition1/users/xcgrun/XCG3/GenesisII \
  /N/u/xcguser/XCG3/GenesisII \
  /N/u/xcguser/XCG3/GenesisII \
  /N/u/xcguser/XCG3/GenesisII \
  /home/uva_xcg/XCG3/GenesisII \
  /soft/genesis2/XCG3/GenesisII \
)

# finds the install folder for a given host.
function get_install_folder()
{
  local host="$1"; shift
  for ((i=0; i < ${#BES_HOST_LIST[@]}; i++)); do
    local listhost="${BES_HOST_LIST[$i]}"
    if [ $listhost == "$host" ]; then
      echo "${BES_INSTALL_FOLDERS[$i]}"
      return 0
    fi
  done
  return 1
}

# finds the username for a given host.
function get_username()
{
  local host="$1"; shift
  for ((i=0; i < ${#BES_HOST_LIST[@]}; i++)); do
    local listhost="${BES_HOST_LIST[$i]}"
    if [ $listhost == "$host" ]; then
      echo "${BES_USER_LIST[$i]}"
      return 0
    fi
  done
  return 1
}

# this takes one host from the BES_HOST_LIST and runs a set of operations on it.
function run_commands_on_bes()
{
  local host="$1"; shift

  local username=$(get_username "$host")

  if [ -z "$username" ]; then
    echo "The host $host could not be found in the BES host list."
    return 1
  fi

  echo found host $host in list with user $username 

  # cranks up the container control command on the remote BES.
  if [[ "$host" =~ .*uc.futuregrid.org ]]; then
    # futuregrid has an extra step before becoming the xcg user.
    echo found special case for future grid BES.
    local extra_cmd="sudo -i -u xcguser"
    # we also need an extra exit command to bail out of the inner shell.
    run_command_remotely "$host" "$username" "$extra_cmd" "${@}" "exit"
    return $?
  else
    # just a normal host.
    run_command_remotely "$host" "$username" "${@}"
    return $?
  fi
}

# this takes one host from the BES_HOST_LIST and controls it according
# to the command parameter.  the available commands are: start, stop,
# and restart.
control_bes_container()
{
  local host="$1"; shift
  local cmd="$1"; shift
  local install_folder=$(get_install_folder "$host")
  run_commands_on_bes "$host" "$install_folder/GFFSContainer $cmd"
}

#test 
#control_bes_container login2.uc.futuregrid.org status
#control_bes_container 149.165.146.134 status

# iterates through all of the known BES and performs the command on their container.
control_all_bes()
{
  local cmd="$1"; shift
  local i
  for ((i=0; i < ${#BES_HOST_LIST[@]}; i++)); do
    local listhost="${BES_HOST_LIST[$i]}"
    if [ "$listhost" == "fir.itc.virginia.edu" ]; then
      matching="$(ifconfig | grep "[: ]128\.143\.")"
      if [ -z "$matching" ]; then
        echo special case for fir--abandoning call because not inside uva network.
        continue
      fi
    fi
    control_bes_container "$listhost" "$cmd"
  done
}

#test
#control_all_bes status

# iterates through all of the known BES and runs a set of commands on each.
run_on_all_bes()
{
  local i
  for ((i=0; i < ${#BES_HOST_LIST[@]}; i++)); do
    local listhost="${BES_HOST_LIST[$i]}"
    if [ "$listhost" == "fir.itc.virginia.edu" ]; then
      matching="$(ifconfig | grep "[: ]128\.143\.")"
      if [ -z "$matching" ]; then
        echo special case for fir--abandoning call because not inside uva network.
        continue
      fi
    fi
    run_commands_on_bes "$listhost" "${@}"
  done
}

# checks if the command passed is for containers or something else.
function is_container_command()
{
  cmd="$1"; shift
  if [ "$cmd" == "stop" -o "$cmd" == "start" -o "$cmd" == "restart" -o "$cmd" == "status" ]; then
    # yes that is one.
    return 0
  else
    # nope, this is not a container command.
    return 1
  fi
}

##############

# main script function: take a BES host and a command for the container on that host.
hostname="$1"; shift
cmd="$1"; shift

if [ -z "$hostname" -o -z "$cmd" ]; then
  echo "This command will perform a BES container action.  It needs two parameters:"
  echo "a BES host and a command.  The BES host can be 'all' to act on all known BES."
  echo "The container control commands are: start, stop, restart, and status."
  echo "Additional commands include: dftmp, cleantmp, "
  exit 1
fi

if is_container_command $cmd; then
  # that was a command for containers.
  if [ "$hostname" == "all" ]; then
    control_all_bes "$cmd"
    exit $?
  else
    control_bes_container "$hostname" "$cmd"
    exit $?
  fi
else
  # the command was for something else.
  cmd_to_run=
  if [ "$cmd" == "dftmp" ]; then
    cmd_to_run="df /tmp"
  fi
  if [ "$cmd" == "cleantmp" ]; then
    cmd_to_run="find /tmp -name 'Axis*att' -type f -mmin +30 -exec rm -v {} ';' 2>/dev/null | echo ok"
  fi

  if [ -z "$cmd_to_run" ]; then
    echo "The command was not understood: '$cmd'"
    exit 1
  fi

  # multiplex the command if desired.
  if [ "$hostname" == "all" ]; then
    run_on_all_bes "$cmd_to_run"
    exit $?
  else
    run_commands_on_bes "$hostname" "$cmd_to_run"
    exit $?
  fi

fi


