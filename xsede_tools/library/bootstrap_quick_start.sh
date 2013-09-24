#!/bin/bash

# coded to build a bootstrapped grid on a fast local partition.
#
# Author: Chris Koeritz

# standard start-up boilerplate.
export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

# make sure we can find our tests.
if [ ! -d "$XSEDE_TEST_ROOT" ]; then
  # clearly the test scripts are not out there.
  XSEDE_TEST_ROOT="$WORKDIR/.."
  if [ ! -d "$XSEDE_TEST_ROOT" ]; then
    echo "Could not auto-locate the xsede tests.  Please set XSEDE_TEST_ROOT."
    exit 1
  fi
fi

# set this variable so the prepare tests script doesn't start a subshell.
GRITTY_TESTING_TOP_LEVEL="$XSEDE_TEST_ROOT"
# pull this stuff in again, just in case all we were given was a test root.
source $XSEDE_TEST_ROOT/prepare_tests.sh 
if [ -z "$TEST_TEMP" ]; then
  echo The auto-location feature failed to find the xsede tests
  exit 1
fi

# drop running guys.
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh
if [ $? -ne 0 ]; then echo "===> script failure, exiting."; exit 1;  fi
sleep 2  # pause just a bit to clear out the dead wood.

# at this point, loading the normal inputs shouldn't hose us, and we need some more
# functions from the test scripts...
source $XSEDE_TEST_ROOT/library/establish_environment.sh

echo -e "** After establishing test environment:\n\tGENII_INSTALL_DIR is $GENII_INSTALL_DIR\n\tGENII_USER_DIR is $GENII_USER_DIR"

##############

# load passwords if they've seen fit to give us any.
if [ -f "$HOME/.secrets/grid_passwords.txt" ]; then 
  source "$HOME/.secrets/grid_passwords.txt"
fi

# set defaults for any passwords we didn't find a value for.
if [ -z "$ADMIN_ACCOUNT_PASSWD" ]; then ADMIN_ACCOUNT_PASSWD="admin"; fi
if [ -z "$NORMAL_ACCOUNT_PASSWD" ]; then NORMAL_ACCOUNT_PASSWD="FOOP"; fi

##############

containerlog="$(get_container_logfile "$DEPLOYMENT_NAME")"
clientlog="$(get_client_logfile "$DEPLOYMENT_NAME")"
# clean out old state directory and old logs.
\rm -rf "$GENII_USER_DIR" "$BACKUP_USER_DIR" \
  "$GENII_INSTALL_DIR/webapps/axis/WEB-INF/attachments"/* \
  "${containerlog}" "${containerlog}".[0-9]* \
  "${clientlog}" "${clientlog}".[0-9]*

# bootstrap and configure a bunch of rights for the admin account.
bash $XSEDE_TEST_ROOT/library/configure_root_container.sh "$USERS_LOC/admin" "$ADMIN_ACCOUNT_PASSWD" $SUBMIT_GROUP $(basename $CONTAINERPATH)
check_if_failed "bootstrap procedure"
#if [ $? -ne 0 ]; then echo "===> script failure, exiting."; exit 1;  fi

export NON_INTERACTIVE=true
bash $XSEDE_TEST_ROOT/library/setup_test_infrastructure.sh $CONTAINERPATH $NORMAL_ACCOUNT_PASSWD
check_if_failed "setting up test infrastructure"
#if [ $? -ne 0 ]; then echo "===> script failure, exiting."; exit 1;  fi

# set up the RNSPATH folder for testing.
#already done: grid mkdir --parents grid:$RNSPATH &>/dev/null
grid chmod -R grid:$RNSPATH +rwx $USERPATH
check_if_failed Could not give $USERPATH permission to the work area $RNSPATH

# logout and get into normal garb.
grid logout --all 
grid login --username=$(basename $USERPATH) --password=$NORMAL_ACCOUNT_PASSWD
check_if_failed "logging in as $USERPATH"
#if [ $? -ne 0 ]; then echo "===> script failure, exiting."; exit 1;  fi

# now add a second container for replication if desired.
bash $XSEDE_TEST_ROOT/library/configure_mirror_container.sh $NORMAL_ACCOUNT_PASSWD
check_if_failed "deploying mirror container"

# stop the container again so we can snapshot the config.
echo "Stopping the container and making a snapshot of the user directory..."
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh

#hmmm: could use a variable for where this file lives.
save_grid_data $TMP/bootstrap_save.zip

# launch the containers again to leave things going.
launch_container_if_not_running "$DEPLOYMENT_NAME"
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" -a ! -z "$BACKUP_USER_DIR" ]; then
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir
fi

echo "All done setting up the container and logging in as user '$(basename $USERPATH)'."

check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

echo "Bootstrap finished at $(date)"

