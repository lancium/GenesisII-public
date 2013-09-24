#!/bin/bash

# this locates the grid container process if possible.  if we cannot find it,
# then the process is restarted.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh &>/dev/null
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh &>/dev/null

launch_container_if_not_running "$DEPLOYMENT_NAME"
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" -a ! -z "$BACKUP_USER_DIR" ]; then
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir
fi

