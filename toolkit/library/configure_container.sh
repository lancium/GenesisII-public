#!/bin/bash

# Configures a new container, including starting it and setting permissions. If the container already exists,
# it checks if it is running. If so it exits with an error message. If it already exists and is not running,
# it wipes it out and builds a new one.
# Usage: configure_container.sh deploymentName hostIPorDNS port

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"


echo "Parameters are $1 $2 $3 $4"

if [ "$#" -ne 4 ]; then
	echo "****************** Error *******************"
	echo "This command takes four parameters: the resource dir path (e.g. /resources/xsede.org/containers), the configuration name, the hostname or IP, and the port"
	echo "****************** Error *******************"
	exit
fi

export RESOURCE_DIR_PATH=$1
export CONTAINER_NAME=$2
export CONTAINER_IP=$3
export CONTAINER_PORT=$4

#user_password="$1"; shift

#if [ -z "$user_password" ]; then
  #echo "This script requires the user's password (for the USERPATH specified"
  #echo "in $GFFS_TOOLKIT_CONFIG_FILE)."
  #exit 1
#fi

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../prepare_tools.sh ../prepare_tools.sh 
fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

echo -e "\nSetting up new container  $CONTAINER_NAME.\n"
echo "Deployment root is ${DEPLOYMENTS_ROOT}"
# Below we ASSUME that the prefix for GENII_USER_DIR should be used for the new working dir
export CONTAINER_USER_DIR="$(dirname ${GENII_USER_DIR})/.genesisII-2.0-${CONTAINER_NAME}"
echo "Container user directory is ${CONTAINER_USER_DIR}"
ddir="${DEPLOYMENTS_ROOT}/${CONTAINER_NAME}"
# Now check if the deployment already exists .. if so, nuke it.
if [ -d "$ddir" ]; then
	echo "Deployment ${CONTAINER_NAME} already exists ****** Cleaning it out"
	# First unlink all of the usual links  -- 
	# /resources/xsede.org/containers/${CONTAINER_NAME}
	# Then, check it is running
	port=$(getDeploymentPort "$CONTAINER_NAME")
	echo "port = $port"
	if [ ! -z "$port" ]; then
		echo "${CONTAINER_NAME} port is ${port}"
		pid=$(pidListeningOnPort "$port")
		if [ ! -z "$pid" ]; then
			echo "${CONTAINER_NAME} pid is ${pid}"
			echo "killing $pid with malice"
			kill -9 "${pid}"
		else
			echo "the container $CONTAINER_NAME is not running"
		fi
	fi
fi

echo "Warning--Erasing deployment directory $ddir"
\rm -rf "$ddir"
\rm -rf $CONTAINER_USER_DIR


# Now create the deployment directory
mkdir "$ddir"
mkdir "${CONTAINER_USER_DIR}"
echo '<deployment-conf based-on="'${DEPLOYMENT_NAME}'"/>' > "${ddir}/deployment-conf.xml"
mkdir "${ddir}/configuration"
wfile="${ddir}/configuration/web-container.properties"
echo "edu.virginia.vcgr.genii.container.listen-port=${CONTAINER_PORT}" > "$wfile"
echo "edu.virginia.vcgr.genii.container.listen-port.use-ssl=true" >> "$wfile"
echo "edu.virginia.vcgr.genii.container.trust-self-signed=true" >> "$wfile"

echo "CONTAINER_USER_DIR is ${CONTAINER_USER_DIR}"

echo "GENII_USER_DIR is ${GENII_USER_DIR}"
cp -f "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" "$CONTAINER_USER_DIR"
echo "Fixing log file and log db in ${CONTAINER_NAME} container logging properties."
replace_phrase_in_file "${CONTAINER_USER_DIR}/build.container.log4j.properties" ".GenesisII.container.log" ".GenesisII\/${CONTAINER_NAME}-container.log"
replace_phrase_in_file "${CONTAINER_USER_DIR}/build.container.log4j.properties" ".GenesisII.logdb.container" ".GenesisII\/logdb.${CONTAINER_NAME}-container"

# get rid of old log files
echo "Executing \rm -r \GenesisII\/${CONTAINER_NAME}-container.log\.*"
\rm -r "GenesisII\/${CONTAINER_NAME}-container.log\.*"

exit


#save_and_switch_userdir "$CONTAINER_USER_DIR"

# fix the logging for the mirror container.  saving this with the state is handled in save and switch now.
#echo "Fixing log file and log db in mirror container logging properties."
#replace_phrase_in_file "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" ".GenesisII.container.log" ".GenesisII\/mirror-container.log"
#replace_phrase_in_file "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" ".GenesisII.logdb.container" ".GenesisII\/logdb.mirror-container"

# make a copy available in the state directory too.
#\cp -f "$GENII_INSTALL_DIR/lib/build.container.log4j.properties" "$BACKUP_USER_DIR"

# clean up any old log.
#\rm -f "$(get_container_logfile "$BACKUP_DEPLOYMENT_NAME")"

echo "Deployment name for backup is ${DEPLOYMENT_NAME}"
launch_container "$DEPLOYMENT_NAME"
#echo "Deployment name for backup is ${BACKUP_DEPLOYMENT_NAME}"
#launch_container "$BACKUP_DEPLOYMENT_NAME"

#restore_userdir

get_root_privileges

multi_grid <<eof
  grid date
  cd ${RESOURCE_DIR_PATH}
  onerror failed to cd to RESOURCE_DIR_PATH
  ln --service-url="https://127.0.0.1:${CONTAINER_PORT}/axis/services/VCGRContainerPortType" "$CONTAINER_NAME"
  onerror failed to link mirror container

  grid date
  # these are the original set of permissions granted to everyone on the mirror container.
  chmod "$CONTAINER_NAME" +rx --everyone
  onerror failed to chmod mirror container for everyone
  chmod "$CONTAINER_NAME"/Services +rx --everyone
  onerror failed to chmod mirror container Services for everyone
  chmod "$CONTAINER_NAME"/Services/GeniiResolverPortType +rx --everyone
  onerror failed to chmod mirror container Resolver port type for everyone
  chmod "$CONTAINER_NAME"/Services/RandomByteIOPortType +rx --everyone
  onerror failed to chmod mirror container RByteIO port type for everyone
  chmod "$CONTAINER_NAME"/Services/EnhancedRNSPortType +rx --everyone
  onerror failed to chmod mirror container RNS port type for everyone

  # permissions added in to enable ACL speedup code; probably not necessary.
#  chmod "$CONTAINER_NAME"/Services/EnhancedNotificationBrokerFactoryPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerFactoryPortType port type for everyone
#  chmod "$CONTAINER_NAME"/Services/EnhancedNotificationBrokerPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerPortType port type for everyone
#
#  chmod "$CONTAINER_NAME"/Services/GeniiPublisherRegistrationPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiPublisherRegistrationPortType port type for everyone
#  
#  chmod "$CONTAINER_NAME"/Services/GeniiPullPointPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiPullPointPortType port type for everyone
#  
#  chmod "$CONTAINER_NAME"/Services/GeniiResolverPortType +rx --everyone
#  onerror failed to chmod mirror container EnhancedNotificationBrokerPortType port type for everyone
#
#  chmod "$CONTAINER_NAME"/Services/GeniiSubscriptionPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiSubscriptionPortType port type for everyone
#
#  chmod "$CONTAINER_NAME"/Services/GeniiWSNBrokerPortType +rx --everyone
#  onerror failed to chmod mirror container GeniiWSNBrokerPortType port type for everyone

  grid date

eof


  #logout --all 
  #login --username=$(basename $USERPATH) --password=$user_password
  #onerror failed to login as expected user
  #grid date

check_if_failed setting up mirror container deployment

