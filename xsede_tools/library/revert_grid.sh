#!/bin/bash

# stops any running container, wipes out its configuration, and undoes a
# supposedly pre-existing file called bootstrap_save.zip to get a saved
# configuration back.  after that, the container is restarted.
#
# Author: Chris Koeritz

# standard start-up boilerplate.
export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

#hmmm: need to use a variable to refer to location for save file.

if [ ! -f "$TMP/bootstrap_save.zip" ]; then
  echo This script will only succeed if a previous bootstrap quick start was done
  echo and that made a file in $TMP/bootstrap_save.zip to revert from.
  exit 1
fi

if [ -z "$GENII_USER_DIR" -o -z "$GENII_INSTALL_DIR" ]; then
  echo Failed to load in the install dir or user dir somehow.
  exit 1
elif [ ! -d "$GENII_INSTALL_DIR" ]; then
  echo The genesis install directory specified is invalid: $GENII_INSTALL_DIR
  exit 1
fi

# drop running guys.
echo zapping any running genesis javas for this user.
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh
if [ $? -ne 0 ]; then echo "===> script failure stopping genesis javas, exiting."; exit 1;  fi
sleep 2
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh

echo cleaning out the test output folder.
rm -rf "$TEST_TEMP"
if [ $? -ne 0 ]; then echo "===> script failure cleaning TEST_TEMP, exiting."; exit 1;  fi
mkdir "$TEST_TEMP"

# get saved grid back.
# whack the state directory and logging for whatever was extant.
\rm -rf "$GENII_USER_DIR" "$HOME/.GenesisII"
if [ $? -ne 0 ]; then echo "===> script failure removing user dir, exiting."; exit 1;  fi
if [ ! -z "$BACKUP_USER_DIR" ]; then
  \rm -rf "$BACKUP_USER_DIR"
  if [ $? -ne 0 ]; then echo "===> script failure removing mirror's user dir, exiting."; exit 1;  fi
fi
umask 077
mkdir "$GENII_USER_DIR"
if [ $? -ne 0 ]; then echo "===> script failure making user dir, exiting."; exit 1;  fi
echo restoring saved grid from zip file.
newdir="$(mktemp -d $TMP/reverting_grid.XXXXXX)"
pushd "$newdir" &>/dev/null
unzip $TMP/bootstrap_save.zip  &>/dev/null
retval=$?
# put back the main user directory.
mv "$(basename $GENII_USER_DIR)"/* $GENII_USER_DIR
retval=$(($retval + $?))
# mirror container's state directory gets put back too.
if [ ! -z "$BACKUP_USER_DIR" ]; then
  mkdir "$BACKUP_USER_DIR"
  mv "$(basename $BACKUP_USER_DIR)"/* $BACKUP_USER_DIR
  retval=$(($retval + $?))
fi
# and restore the deployments directory.
if [ ! -d "$GENII_INSTALL_DIR/deployments" ]; then
  mkdir "$GENII_INSTALL_DIR/deployments"
fi
\cp -r -f -n deployments/* "$GENII_INSTALL_DIR/deployments"
retval=$(($retval + $?))
\rm -rf "$newdir"
if [ $retval -ne 0 ]; then echo "===> script failure cleaning up temporary saved grid, exiting."; exit 1;  fi
popd &>/dev/null

# start container up.
launch_container_if_not_running "$DEPLOYMENT_NAME"
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" -a ! -z "$BACKUP_USER_DIR" ]; then
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir
fi

