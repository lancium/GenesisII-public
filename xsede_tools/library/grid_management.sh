#!/bin/bash

# powerful management functions for the grid or containers.
#
# Author: Chris Koeritz

##############

# the number of lines we scan for the container startup phrase.
export LINE_COUNT_FOR_START_CHECK=200

##############

# general functions that affect client and container.

# this takes one argument, which is a new directory to use for the container
# state.  this must always be followed by a restore_userdir without any
# intervening new call to save_and_switch.
function save_and_switch_userdir()
{
  local new_dir="$1"; shift
  HOLD_USERDIR="$GENII_USER_DIR"
  # new kludge; save the logging config for this user dir.
  cp "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" "$HOLD_USERDIR"
#echo "sasu: saving log props mentioning: $(cat "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" | grep "LOGFILE.File")"
  export GENII_USER_DIR="$new_dir"
  if [ ! -d "$GENII_USER_DIR" ]; then
    # it's handy for this directory to exist before we copy things into it.
    mkdir "$GENII_USER_DIR"
  fi
  if [ -f "$GENII_USER_DIR/genesisII.container.log4j.properties" ]; then
    cp "$GENII_USER_DIR/genesisII.container.log4j.properties" "$GENII_INSTALL_DIR/lib"
#echo "sasu: copying in log props from new user dir $GENII_USER_DIR"
  fi
#echo "sasu: currently using log props mentioning: $(cat "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" | grep "LOGFILE.File")"
}

# restores the previous user directory.  handles one level of rollback.
function restore_userdir()
{
  # new kludge; save the logging config for the current user dir.
  cp "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" "$GENII_USER_DIR"
#echo "ru: saving log props mentioning: $(cat "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" | grep "LOGFILE.File")"
  export GENII_USER_DIR="$HOLD_USERDIR"
  if [ -f "$GENII_USER_DIR/genesisII.container.log4j.properties" ]; then
#echo "ru: copying in log props from restoring user dir $GENII_USER_DIR"
    cp "$GENII_USER_DIR/genesisII.container.log4j.properties" "$GENII_INSTALL_DIR/lib"
  fi
#echo "ru: now using log props mentioning: $(cat "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" | grep "LOGFILE.File")"
}

##############

# container related functions.

# starts up the genesis2 container on this machine if it is not found running.
# takes an optional deployment name.  note that it is crucial that you have
# switched to the right user directory if you are trying to launch a mirror
# container; see the save_and_switch_userdir method.
function launch_container_if_not_running()
{
  dep="$1"; shift
  # launch the container here if we don't think it's running.
  local running="$(find_genesis_javas $dep)"
  if [ ! -z "$running" ]; then
    echo "The container already seems to be running for this user."
    return
  else
    echo "Grid container not seen running--starting it."
  fi
  launch_container "$dep"
}

# returns the standard location for the container log file, or if this
# is the mirrored deployment, it returns a contrived log file.
function get_container_logfile()
{
  local DEP_NAME="$1"; shift
  local extra=
  if [ -z "$DEP_NAME" ]; then
    DEP_NAME=default
  else
    extra="_${DEP_NAME}"
  fi
#hmmm: this may not be right to remove yet, but we now have a real log file for mirror.
#  if [ "$DEP_NAME" != "default" ]; then
#    # this is kludgy, but we need to separate logging for mirror containers...
#    # and right now we consider anything we'd launch which is not called
#    # "default" to be a mirror container.
#    echo "$TEST_TEMP/container_$(hostname)_${USER}$extra.log"
#    return 0
#  fi
  # log file for normal deployments.
  local logfile="$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties"
  if [ "$DEP_NAME" == "$BACKUP_DEPLOYMENT_NAME" ]; then
    # trying to be somewhat clever and use the state directory if it has log4j properties.
    if [ -f "$BACKUP_USER_DIR/genesisII.container.log4j.properties" ]; then
      logfile="$BACKUP_USER_DIR/genesisII.container.log4j.properties"
    else
      # if we cannot find the actual log4j props, don't return a name that would be
      # the same as the main container log.
      return
    fi
  elif [ "$DEP_NAME" == "default" ]; then
    if [ -f "$GENII_USER_DIR/genesisII.container.log4j.properties" ]; then
      logfile="$GENII_USER_DIR/genesisII.container.log4j.properties"
    fi
  fi
  to_return="$(grep log4j.appender.LOGFILE.File "$logfile" | tr -d '\r\n' | sed -e 's/.*=\(.*\)/\1/' | sed -e "s%\${user.home}%$HOME%")"
  echo "$to_return"
}

# returns the standard location for the client log file.
function get_client_logfile()
{
  to_return="$(grep log4j.appender.LOGFILE.File $GENII_INSTALL_DIR/lib/genesisII.client.log4j.properties | tr -d '\r\n' | sed -e 's/.*=\(.*\)/\1/' | sed -e "s%\${user.home}%$HOME%")"
  echo "$to_return"
}

# just barrels ahead and tries to launch the container.  takes an optional
# deployment name.  note that it is crucial to switch to the right user
# directory if trying to launch a mirror container; see the
# save_and_switch_userdir method for details.
function launch_container()
{
  local DEP_NAME="$1"; shift

  # move the log out of the way so we don't get fooled by old startup noise.
  containerlog="$(get_container_logfile "$DEP_NAME")"

  # ensure that we have at least our scanning factor worth of lines in the buffer
  # that are not the restart phrase.
  local d;
  if [ -f "$containerlog" ]; then
    for ((d=0; d < $LINE_COUNT_FOR_START_CHECK; d++)); do
      echo "-" >>"$containerlog"
    done
  fi

  pushd "$GENII_INSTALL_DIR" &>/dev/null

  echo "Launching Genesis II container for deployment \"$DEP_NAME\"..."
  CONTAINERLOGFILE="$(get_container_logfile "$DEP_NAME")"
  echo "$DEP_NAME container log stored at: $CONTAINERLOGFILE"
#  redirection=0
#  if [ "$DEP_NAME" != "default" ]; then
#    # if we're not a default deployment, we need to redirect output to save it.
#    redirection=1
#  fi
  extra_prefix=
  extra_suffix=
  use_shell=bash
  runner="$GENII_INSTALL_DIR/runContainer.sh"
  if [ ! -f "$runner" ]; then
    runner=$GENII_INSTALL_DIR/GFFSContainer
    extra_suffix="start"
  fi
  if [ ! -f "$runner" ]; then
    use_shell=
    runner="$GENII_INSTALL_DIR/wrapper-windows-x86-32.exe"
    extra_suffix="$GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/conf/wrapper.conf"
  fi
  if [ ! -f "$runner" ]; then
    runner="$(echo $GENII_INSTALL_DIR/runContainer.bat | sed -e 's/\//\\\\/g')"
    use_shell=cmd
    extra_suffix=
    if [ -f "$runner" ]; then
      extra_prefix="/c"
    fi
  fi
  if [ ! -f "$runner" ]; then
    echo "Failed to find the launcher for Genesis II."
    exit 1
  fi

#echo "shell=$use_shell extra_prefix=$extra_prefix runner=$runner extra_suffix=$extra_suffix"
#  if [ $redirection -eq 0 ]; then
    $use_shell $extra_prefix $runner $extra_suffix $DEP_NAME &>/dev/null &
#  else
#    $use_shell $extra_prefix $runner $extra_suffix $DEP_NAME &>$CONTAINERLOGFILE &
#  fi

  # snooze to allow the container to get going.  the counter measures number of 10 second
  # sleeps to allow.
  i=20
  while [ -z "$(if [ -f $CONTAINERLOGFILE ]; then tail -n $LINE_COUNT_FOR_START_CHECK < $CONTAINERLOGFILE | grep "Restarting all BES Managers"; fi)" ]; do
    if [ $i -le 0 ]; then
      echo "Failed to find proper phrasing in container log to indicate it started; continuing anyway."
      break;
    fi
    echo "Pausing to await container start..."
    sleep 10
    i=$(expr $i - 1)
  done
  echo "$DEP_NAME container has started."
  popd &>/dev/null
}

##############

# this saves the grid deployments and user data for the normal container and
# the mirror, if enabled.
function save_grid_data()
{
  local backup_file="$1"; shift
  if [ -z "$backup_file" ]; then
    echo "This function requires a backup file name for storing the grid data."
    return 1
  fi
  \rm -f "$backup_file"

  bash $XSEDE_TEST_ROOT/library/backup_container_state.sh "$backup_file"
  if [ $? -ne 0 ]; then echo "===> script failure backing up container state."; return 1; fi

  return 0

#older approach.
  # now grab up a copy of the normal state directory.
  # we need to zip it without a full path, so we can easily unzip it to the right place.
  pushd $GENII_USER_DIR/.. &>/dev/null
  zip -r $HOME/bootstrap_save.zip "$(basename "$GENII_USER_DIR")" &>/dev/null
  popd &>/dev/null
  # zip the mirror container's state too, if there is one.
  if [ ! -z "$BACKUP_USER_DIR" ]; then
    pushd $BACKUP_USER_DIR/.. &>/dev/null
    zip -r $HOME/bootstrap_save.zip "$(basename "$BACKUP_USER_DIR")" &>/dev/null
    popd &>/dev/null
  fi
  # and back up the deployment info.
  pushd "$DEPLOYMENTS_ROOT/.." &>/dev/null
  zip -r $HOME/bootstrap_save.zip deployments &>/dev/null
  popd &>/dev/null
  \mv $HOME/bootstrap_save.zip "$backup_file"
}

##############

# helper functions for the mirror container.

# checks if the mirror container is enabled in the configuration.
function isMirrorEnabled()
{
  if [ -z "$BACKUP_DEPLOYMENT_NAME" -o -z "$BACKUP_USER_DIR" -o -z "$BACKUP_USER_DIR" \
      -o -z "$BACKUP_PORT_NUMBER" ]; then
    return 1  # not enabled.
  else
    return 0  # zero exit is success; is enabled.
  fi
}

##############

# grid-wide operations.

# runs the bootstrap XML script to establish our basic grid configuration.
function bootstrap_grid()
{
  # go to the folder for these steps due to some squirreliness.
  pushd $GENII_INSTALL_DIR &>/dev/null

  # perform the basic setup of the grid, already canned for us.
  echo "Bootstrapping default grid configuration..."

  bootstrap_file="$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/configuration/bootstrap.xml"
  if [ $NAMESPACE == 'xsede' ]; then
    echo -e "\n---- Choosing 'xsede' namespace for bootstrap ----"
    cp "$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/configuration/xsede-bootstrap.xml" $bootstrap_file
  elif [ $NAMESPACE == 'xcg' ]; then
    echo -e "\n---- Choosing 'xcg' namespace for bootstrap ----"
    cp "$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/configuration/xcg-bootstrap.xml" $bootstrap_file
  else
    echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
    exit 1
  fi

  # fix the bootstrap to point to the right deployments folder.
  replace_phrase_in_file $bootstrap_file '${GENII_INSTALL_DIR}/deployments' "${DEPLOYMENTS_ROOT}"

  # if possible, fix the password for the admin account.
  replace_phrase_in_file $bootstrap_file "password=keys" "password=$ADMIN_ACCOUNT_PASSWD"

  grid_chk script "local:${bootstrap_file}"

  # jump back out of the install directory.  the deployment behaves
  # oddly if we aren't in there, but nothing else should require being in the install dir.
  popd &>/dev/null
}

##############

# scripts for managing queues, BESes, resources, etc.

# sets up a BES on a container, where the full path to the container is expected (e.g.
# "/containers/busby" rather than just "busby").  the second parm should be the number
# of instances of the BES to create.
function create_BES()
{
  local container_name="$1"; shift
  local owner_name="$1"; shift
  local queue_name="$1"; shift
  local group_name="$1"; shift
  local bes_name="$1"; shift  # optional

  local short_container="$(basename $container_name)"

  if [ -z "$bes_name" ]; then
    bes_name="$BES_CONTAINERS_LOC/${short_container}-bes"
  fi

  if [ -z "$container_name" -o -z "$owner_name" -o -z "$queue_name" -o -z "$group_name" ]; then
    echo "create_BES function needs a container name to operate on, a user to give access to the"
    echo "container, a queue to also give access, and a group to also give access."
    exit 1
  fi

  echo "Creating a BES at $bes_name"

  multi_grid <<eof
    create-resource "$container_name/Services/GeniiBESPortType" "$bes_name"
    chmod "$bes_name" +rwx "$owner_name"
    chmod "$bes_name" +rx "$queue_name"
    chmod "$bes_name" +rx "$group_name"
eof
  check_if_failed "Creating a BES on the container '$container_name'"
}

# establishes a queue given the full path to a container name.
function create_queue()
{
  local queue_path="$1"; shift
  local container_name="$1"; shift
  local owner_name="$1"; shift
  local group_name="$1"; shift

  if [ -z "$queue_path" -o -z "$container_name" -o -z "$owner_name" -o -z "$group_name" ]; then
    echo "create_queue function needs a path to put the queue, the container"
    echo "name to operate on, the owner name for the queue, and a user name"
    echo "to give access to the queue."
    exit 1
  fi

  echo "Creating a queue at $queue_path"

  multi_grid <<eof
    create-resource "$container_name/Services/QueuePortType" "$queue_path"
    chmod "$queue_path" +rwx "$owner_name"
    chmod "$queue_path" +rx "$group_name"
eof
  check_if_failed "Creating queue resource on container '$container_name'"
}

# takes a BES resource and links it under the queue in question.
# the resource is given the specified number of slots for processing jobs.
function give_queue_a_resource()
{
  local queue_name="$1"; shift
  local bes_name="$1"; shift
  local queue_slots="$1"; shift
  if [ -z "$bes_name" -o -z "$queue_name" -o -z "$queue_slots" ]; then
    echo "give_queue_a_resource function needs queue name, bes resource name, and slot count."
    exit 1
  fi

  echo "Giving queue a BES from $bes_name with $queue_slots slots"

  local short_bes="$(basename $bes_name)"
  multi_grid <<eof
    ln "$bes_name" "$queue_name/resources/$short_bes"
    qconfigure "$queue_name" "$short_bes" "$queue_slots"
eof
  check_if_failed Adding BES to queue $queue_name and setting slot count.
}

##############

declare -a genesis_java_pids=()

# locates any running processes that seem to be from genesis 2.
function find_genesis_javas()
{
  pattern="$1"; shift
  # now a cascade of attempts to find some processes.
  user=$USER
  if [ "$OS" == "Windows_NT" ]; then
    unset user
  fi
  temp_array=()
  # make sure we match the user-defined pattern also.
  addon=".*$pattern"
  local patterns=""
  # build a list of the true patterns we want.
  for i in "java.*genesis[^2][^-][^g][^f]" "java.*genii-" "JavaServiceWrapper" "JavaSe.*wrapper.*windows"; do
    patterns+="${i}${addon} "
  done
  # find all the processes matching those patterns.
  genesis_java_pids="$(psfind $patterns)"
}

##############

# helper method that checks for a phrase in a file and reports the number of occurrences.
function show_count()
{
  local phrase="$1"; shift
  local file="$1"; shift
  echo -ne "| $phrase\t"
  echo -ne "$(grep -i "$phrase" "$file"* | wc -l)\t"
}

# makes a report of successes and failures found in the container and client logs.
function check_logs_for_errors()
{
  local DEP_NAME="$1"; shift
  if [ -z "$DEP_NAME" ]; then
    DEP_NAME=default
  fi
  local file="$(get_container_logfile "$DEP_NAME")"
  if [ -f "$file" ]; then
    echo "Log File: $file"
    for i in fail warn error grant; do
      show_count "$i" "$file"
    done
    echo "|"
  fi
  # only print client stats for the main container.
  if [ "$DEP_NAME" == "default" ]; then
    file="$(get_client_logfile)"
    if [ -f "$file" ]; then
      echo "Log File: $file"
      for i in fail warn error; do
        show_count "$i" "$file"
      done
      echo "|"
    fi
  fi
}

##############

