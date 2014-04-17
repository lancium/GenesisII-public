#!/bin/bash

# establishes a mirror container for the root, but does not add any resolvers or replication yet.
#
# Author: Salvatore Valente
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

user_password="$1"; shift

if [ -z "$user_password" ]; then
  echo "This script requires the user's password (for the USERPATH specified"
  echo "in $XSEDE_TOOLS_CONFIG_FILE)."
  exit 1
fi

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# check if we have a config for the mirror container.
if [ -z "$BACKUP_DEPLOYMENT_NAME" -o -z "$BACKUP_USER_DIR" \
    -o -z "$BACKUP_CONTAINER" -o -z "$BACKUP_PORT_NUMBER" ]; then
  echo -e "\nNot setting up mirror container for replication testing.\n"
  exit 0
fi

echo -e "\nSetting up mirror container for replication testing.\n"

if [ "$BACKUP_DEPLOYMENT_NAME" == "default" ]; then
  echo "Failure--mirror's deployment name cannot be 'default'"
  exit 1
fi

ddir="${DEPLOYMENTS_ROOT}/${BACKUP_DEPLOYMENT_NAME}"
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

echo "destroying contents of user dir $BACKUP_USER_DIR"
\rm -rf "$BACKUP_USER_DIR"

save_and_switch_userdir "$BACKUP_USER_DIR"

# fix the logging for the mirror container.  saving this with the state is handled in save and switch now.
echo "Fixing log file and log db in mirror container logging properties."
replace_phrase_in_file "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" ".GenesisII.container.log" ".GenesisII\/mirror-container.log"
replace_phrase_in_file "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" ".GenesisII.logdb.container" ".GenesisII\/logdb.mirror-container"

# make a copy available in the state directory too.
\cp -f "$GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties" "$BACKUP_USER_DIR"

# clean up any old log.
\rm -f "$(get_container_logfile "$BACKUP_DEPLOYMENT_NAME")"

launch_container "$BACKUP_DEPLOYMENT_NAME"

restore_userdir

get_root_privileges

multi_grid <<eof
  ln --service-url="https://127.0.0.1:${BACKUP_PORT_NUMBER}/axis/services/VCGRContainerPortType" "$BACKUP_CONTAINER"
  onerror failed to link mirror container

  chmod "$BACKUP_CONTAINER" +rx --everyone
  onerror failed to chmod mirror container for everyone
  chmod "$BACKUP_CONTAINER"/Services +rx --everyone
  onerror failed to chmod mirror container Services for everyone
  chmod "$BACKUP_CONTAINER"/Services/GeniiResolverPortType +rx --everyone
  onerror failed to chmod mirror container Resolver port type for everyone
  chmod "$BACKUP_CONTAINER"/Services/RandomByteIOPortType +rx --everyone
  onerror failed to chmod mirror container RByteIO port type for everyone
  chmod "$BACKUP_CONTAINER"/Services/EnhancedRNSPortType +rx --everyone
  onerror failed to chmod mirror container RNS port type for everyone

  logout --all 
  login --username=$(basename $USERPATH) --password=$user_password
  onerror failed to login as expected user
eof
check_if_failed setting up mirror container deployment

