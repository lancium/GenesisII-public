#!/bin/bash

# coded to build a bootstrapped grid on a fast local partition.
#
# Author: Chris Koeritz

# establishes variables needed by the deployment generator for our test
# grid to be bootstrapped.
function setup_key_bootstrapping_variables()
{
  # definitions of the certificate file names, passwords and aliases.
  export TRUSTSTORE_PASSWORD='trusted'
  export TRUSTSTORE_PFX=trusted.pfx

  export TLS_IDENTITY_PFX=tls-cert.pfx
  if [ -z "$TLS_IDENTITY_PASSWORD" ]; then
    export TLS_IDENTITY_PASSWORD=tilly
  fi

  export CA_ALIAS='signing-cert'
  export CA_PFX=signing-cert.pfx
  export CA_CERT_FILE=signing-cert.cer
  export CA_PASSWORD=signer

  export ADMIN_PFX=admin.pfx
  export ADMIN_CERT_FILE=admin.cer

  export ADMIN_PASSWORD=$ADMIN_ACCOUNT_PASSWD
  export ADMIN_ALIAS=skynet

  ##############

  export DEPLOYMENT_MEMORY_FILE=saved-deployment-info.txt

  # try to get the most correct name for the current host.
  export MACHINE_NAME=$(hostname -f)
  export PORT=18080

  export NEW_DEPLOYMENT=$DEPLOYMENT_NAME
}

# standard start-up boilerplate.
export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

# make sure we can find our tests.
if [ ! -d "$GFFS_TOOLKIT_ROOT" ]; then
  # clearly the test scripts are not out there.
  GFFS_TOOLKIT_ROOT="$WORKDIR/.."
  if [ ! -d "$GFFS_TOOLKIT_ROOT" ]; then
    echo "Could not auto-locate the xsede tests.  Please set GFFS_TOOLKIT_ROOT."
    exit 1
  fi
fi

# set this variable so the prepare tests script doesn't start a subshell.
GRITTY_TESTING_TOP_LEVEL="$GFFS_TOOLKIT_ROOT"
# pull this stuff in again, just in case all we were given was a test root.
source "$GFFS_TOOLKIT_ROOT/prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The auto-location feature failed to find the xsede tests
  exit 1
fi

# drop running guys.
bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh"
if [ $? -ne 0 ]; then echo "===> script failure, exiting."; exit 1;  fi
sleep 2  # pause just a bit to clear out the dead wood.

# at this point, loading the normal inputs shouldn't hose us, and we need some more
# functions from the test scripts...
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

echo "** After establishing test environment:"
var GENII_INSTALL_DIR GENII_USER_DIR NAMESPACE_FILE
#hmmm: temp on namespace file

# clean up any old logging or logging dbs.
\rm -rf $HOME/.GenesisII

# clean up any conglomerated log file.
\rm -f "$CONGLOMERATED_GRID_OUTPUT"

##############

# load vars for the deployment generator.
setup_key_bootstrapping_variables

# toss out any previously existing bootstrap script, since we want to write
# a new one.
\rm -f "$ACTUAL_DEPLOYMENT_FOLDER/configuration/bootstrap.xml" 

containerlog="$(get_container_logfile "$DEPLOYMENT_NAME")"
clientlog="$(get_client_logfile "$DEPLOYMENT_NAME")"
# clean out old state directory and old logs.
\rm -rf "$GENII_USER_DIR" \
  "$GENII_INSTALL_DIR/webapps/axis/WEB-INF/attachments"/* \
  "${containerlog}" "${containerlog}".[0-9]* \
  "${clientlog}" "${clientlog}".[0-9]*
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" -a ! -z "$BACKUP_USER_DIR" ]; then
  # clean out mirror container cruds also.
  mirrorlog="$(get_container_logfile "$BACKUP_DEPLOYMENT_NAME")"
  \rm -rf "$BACKUP_USER_DIR" \
    "${mirrorlog}" "${mirrorlog}".[0-9]*
fi

# bootstrap and configure a bunch of rights for the admin account.
bash "$GFFS_TOOLKIT_ROOT/library/configure_root_container.sh" "$USERS_LOC/admin" "$ADMIN_ACCOUNT_PASSWD" $SUBMIT_GROUP $(basename $CONTAINERPATH)
check_if_failed "bootstrap procedure"

# copy up a bogus deployment information file to have a placeholder that makes sense.
cp "$GENII_INSTALL_DIR/installer/bootstrap-build.config" "$GENII_INSTALL_DIR/current.deployment"
check_if_failed "copying current.deployment for bootstrapped grid"

export NON_INTERACTIVE=true
bash "$GFFS_TOOLKIT_ROOT/library/setup_test_infrastructure.sh" $CONTAINERPATH $NORMAL_ACCOUNT_PASSWD
check_if_failed "setting up test infrastructure"

# set up the RNSPATH folder for testing.
silent_grid chmod -R grid:$RNSPATH +rwx $USERPATH
check_if_failed Could not give $USERPATH permission to the work area $RNSPATH

# logout and get into normal garb.
silent_grid logout --all 
silent_grid login --username=$(basename $USERPATH) --password=$NORMAL_ACCOUNT_PASSWD
check_if_failed "logging in as $USERPATH"

# now add a second container for replication if desired.
bash "$GFFS_TOOLKIT_ROOT/library/configure_mirror_container.sh" $NORMAL_ACCOUNT_PASSWD
check_if_failed "deploying mirror container"

#maybe remove
echo after configuring mirror container...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

# stop the container again so we can snapshot the config.
echo "Stopping the container and making a snapshot of the user directory..."
bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh"

#hmmm: could use a variable for where this file lives.
save_grid_data $TMP/bootstrap_save.tar.gz

# launch the containers again to leave things going.
launch_container_if_not_running "$DEPLOYMENT_NAME"
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" -a ! -z "$BACKUP_USER_DIR" ]; then
  save_and_switch_userdir "$BACKUP_USER_DIR"
  save_grid_data $TMP/mirror_save.tar.gz
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir
fi

echo "All done setting up the container and logging in as user '$(basename $USERPATH)'."

check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

echo "Bootstrap finished at $(date)"

