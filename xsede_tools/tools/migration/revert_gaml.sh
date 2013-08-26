#!/bin/bash

# this script needs a copy of these items in the folder where it will run:
#
#   deployments directory (with a deployment for backup also).
#   ~/.genesisII-2.0 state directory.
#   ~/.tmp/dot-genesis-backup state directory.
#   gaml grid's context.xml file (from $GENII_INSTALL_DIR).
#

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 1; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

function check_result()
{
  if [ $? -ne 0 ]; then
    echo -e "failed on: $*"
    exit 1
  fi
}

pushd $WORKDIR &>/dev/null
check_result "changing to our own directory"

bash $GENII_INSTALL_DIR/xsede_tools/library/zap_genesis_javas.sh

\rm -rf $HOME/.GenesisII
\rm -rf $HOME/.genesisII-2.0
\rm -rf $HOME/.tmp/dot-genesis-backup

cp -R .genesisII-2.0 $HOME/.genesisII-2.0
check_result "copying state directory into place"

cp -R dot-genesis-backup $HOME/.tmp/dot-genesis-backup
check_result "copying backup state directory into place"

synch_files deployments/default/security $GENII_INSTALL_DIR/deployments/default/security
check_result "synchronizing security directories"

synch_files deployments/backup $GENII_INSTALL_DIR/deployments/backup
check_result "synchronizing backup deployment"

cp context.xml $GENII_INSTALL_DIR
check_result "copying context into place"

popd &>/dev/null

launch_container_if_not_running "$DEPLOYMENT_NAME"

save_and_switch_userdir "$BACKUP_USER_DIR"
launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
restore_userdir

echo "tailing log now, can quit anytime."
tail -f $HOME/.GenesisII/container.log

