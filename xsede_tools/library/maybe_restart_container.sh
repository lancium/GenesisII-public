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

# see if the process is findable.
# (this approach will not work if the process actually freezes up but
# is still present.  we'll never notice the problem.  to catch that, we
# could be checking the last update time on the main log file.)
find_genesis_javas "$DEPLOYMENT_NAME"
#echo "got list: ${genesis_java_pids[*]}"

if [ ${#genesis_java_pids[*]} -eq 0 ]; then
  # jump into the genesis directory so we can run stuff.
#  pushd $GENII_INSTALL_DIR &>/dev/null

  echo -e "\n$(date): Restarting container instance on $(hostname)."

  launch_container_if_not_running "$DEPLOYMENT_NAME"

  echo "$(date): Restarted container instance on $(hostname)."
  echo
  popd &>/dev/null

#else echo "$(date): did nothing--container is already running."

fi

