#!/bin/bash

# establishes a mirror container for the root, but does not add any resolvers or replication yet.
#
# Author: Salvatore Valente
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

# Allow coloring of echo commands
RED='\033[0;31m'
NC='\033[0m' # No Color

user_password="$1"; shift

if [ -z "$user_password" ]; then
  echo "This script requires the user's password (for the USERPATH specified"
  echo "in $GFFS_TOOLKIT_CONFIG_FILE)."
  exit 1
fi

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../prepare_tools.sh ../prepare_tools.sh 
fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

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

echo "${RED}Backup_USER_DIR is ${BACKUP_USER_DIR}${NC}"
save_and_switch_userdir "$BACKUP_USER_DIR"

# fix the logging for the mirror container.  saving this with the state is handled in save and switch now.
echo "Fixing log file and log db in mirror container logging properties."
replace_phrase_in_file "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" ".GenesisII.container.log" ".GenesisII\/mirror-container.log"
replace_phrase_in_file "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" ".GenesisII.logdb.container" ".GenesisII\/logdb.mirror-container"

# make a copy available in the state directory too.
\cp -f "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" "$BACKUP_USER_DIR"

# clean up any old log.
\rm -f "$(get_container_logfile "$BACKUP_DEPLOYMENT_NAME")"

echo "${RED}Deployment name for backup is ${BACKUP_DEPLOYMENT_NAME}${NC}"
launch_container "$BACKUP_DEPLOYMENT_NAME"

restore_userdir

get_root_privileges

multi_grid <<eof
  grid date
  ln --service-url="https://127.0.0.1:${BACKUP_PORT_NUMBER}/axis/services/VCGRContainerPortType" "$BACKUP_CONTAINER"
  onerror failed to link mirror container

  grid date
  # these are the original set of permissions granted to everyone on the mirror container.
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

  # permissions added in to enable ACL speedup code; probably not necessary.
#  chmod "$BACKUP_CONTAINER"/Services/EnhancedNotificationBrokerFactoryPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerFactoryPortType port type for everyone
#  chmod "$BACKUP_CONTAINER"/Services/EnhancedNotificationBrokerPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerPortType port type for everyone
#
#  chmod "$BACKUP_CONTAINER"/Services/GeniiPublisherRegistrationPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiPublisherRegistrationPortType port type for everyone
#  
#  chmod "$BACKUP_CONTAINER"/Services/GeniiPullPointPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiPullPointPortType port type for everyone
#  
#  chmod "$BACKUP_CONTAINER"/Services/GeniiResolverPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerPortType port type for everyone
#
#  chmod "$BACKUP_CONTAINER"/Services/GeniiSubscriptionPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiSubscriptionPortType port type for everyone
#
#  chmod "$BACKUP_CONTAINER"/Services/GeniiWSNBrokerPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiWSNBrokerPortType port type for everyone

  grid date
  logout --all 
  login --username=$(basename $USERPATH) --password=$user_password
  onerror failed to login as expected user
  grid date
eof
check_if_failed setting up mirror container deployment

