#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

user_password="$1"; shift

if [ -z "$user_password" ]; then
  echo "This script requires the user's password (for the USERPATH specified"
  echo "in inputfile.txt)."
  exit 1
fi

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# Bootstrap backup container for testing replicated resources?
if [ -z "$BACKUP_DEPLOYMENT_NAME" -o -z "$BACKUP_USER_DIR" \
    -o -z "$BACKUP_CONTAINER" -o -z "$BACKUP_PORT_NUMBER" ]; then
  echo -e "\nNot setting up backup container for replication testing.\n"
else
  echo -e "\nSetting up backup container for replication testing.\n"

  # clean up old logs.
  \rm -f "$(get_container_logfile "$BACKUP_DEPLOYMENT_NAME")"

  if [ "$BACKUP_DEPLOYMENT_NAME" == "default" ]; then
    echo "Failure--backup deployment name cannot be 'default'"
    exit 1
  fi

  ddir="${GENII_INSTALL_DIR}/deployments/${BACKUP_DEPLOYMENT_NAME}"
  if [ -d "$ddir" ]; then
    echo "Warning--Erasing directory $ddir"
    \rm -rf "$ddir"
  fi
  mkdir "$ddir"
  echo '<deployment-conf based-on="'${DEPLOYMENT_NAME}'"/>' > "${ddir}/deployment-conf.xml"
  mkdir "${ddir}/configuration"
  wfile="${ddir}/configuration/web-container.properties"
  echo "edu.virginia.vcgr.genii.container.listen-port=${BACKUP_PORT_NUMBER}" > "$wfile"
  echo "edu.virginia.vcgr.genii.container.listen-port.use-ssl=true" >> "$wfile"
  echo "edu.virginia.vcgr.genii.container.trust-self-signed=true" >> "$wfile"

  save_and_switch_userdir "$BACKUP_USER_DIR"

  echo "destroying contents of user dir $BACKUP_USER_DIR"
  \rm -rf "$BACKUP_USER_DIR"

  launch_container "$BACKUP_DEPLOYMENT_NAME"

  restore_userdir

  get_root_privileges
  grid_chk ln --service-url="https://127.0.0.1:${BACKUP_PORT_NUMBER}/axis/services/VCGRContainerPortType" "$BACKUP_CONTAINER"

  grid_chk chmod "$LOCAL_CONTAINER"/Services/GeniiResolverPortType +rwx --everyone
  grid_chk chmod "$LOCAL_CONTAINER"/Services/RandomByteIOPortType +rwx --everyone
  grid_chk chmod "$LOCAL_CONTAINER"/Services/EnhancedRNSPortType +rwx --everyone

  grid_chk chmod "$BACKUP_CONTAINER" +rx --everyone
  grid_chk chmod "$BACKUP_CONTAINER"/Services +rx --everyone
  grid_chk chmod "$BACKUP_CONTAINER"/Services/GeniiResolverPortType +rwx --everyone
  grid_chk chmod "$BACKUP_CONTAINER"/Services/RandomByteIOPortType +rwx --everyone
  grid_chk chmod "$BACKUP_CONTAINER"/Services/EnhancedRNSPortType +rwx --everyone

  grid_chk logout --all 
  grid_chk login --username=$(basename $USERPATH) --password=$user_password
fi


