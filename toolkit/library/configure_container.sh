#!/bin/bash

# Configures a new container, including starting it and setting permissions. If the container already exists,
# it checks if it is running. If so it exits with an error message. If it already exists and is not running,
# it wipes it out and builds a new one.
# Usage: configure_container.sh deploymentName hostIPorDNS port
# Author: ASG. 2021-01-28, based on configure_mirror_container.sh
# This code will create a new deployment based on deploymentName, creating, and populating
# the directories 
#	~/.genesisII-2.0-deploymentName -- this is where the container database, rbyteio-data, 
#		and the build.container.log4j.properties, an build.client.log4j.properties go
#	$GENII_INSTALL_DIR/deployments/deploymentName - which includes sub-directories
#		configuration - contains web-container-properties and security properties for the container
#		security - contains admin, owner, TLS, and signing certs, as well sub dir of trusted certs
# After file and directory setup the container will be spun up, and then linked into the gffs
# at /resources/xsede.org/containrs/<deploymentName>.
# Appropriate permissions will be configured.
# Also, because this is intended for use primarily during testing, where the user /users/xsede.org/userX
# we put the userX cer in security/default-owners/owner.cer
# Note: since we are assuming the regression test grid, the admin.cer password is 'tester'


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

#Let's first verify the grid is up, if not exit
grid ls / &> /dev/null

if [ $? -eq 0 ]; then
   echo "OK, the grid is up."
else
   echo "The grid is not up ... aborting."
   exit
fi

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
\rm -rf "$CONTAINER_USER_DIR"
# Now copy the current_grid deployment over into the deployments dir
cp -r ${DEPLOYMENTS_ROOT}/current_grid $ddir
# Make the deployments user dir. This is where stuff like the database, rbyteio dir, etc go
mkdir "${CONTAINER_USER_DIR}"

# Now we overwrite the deployment-conf.xml file with new information

echo '<deployment-conf based-on="'${DEPLOYMENT_NAME}'"/>' > "${ddir}/deployment-conf.xml"
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
echo "Executing rm $GENII_USER_DIR/${CONTAINER_NAME}-container.log"
rm  "$GENII_USER_DIR/${CONTAINER_NAME}-container.log"

# fix up the server config with our hostname.
  replace_phrase_in_file "$ddir/configuration/server-config.xml" "\(name=.edu.virginia.vcgr.genii.container.external-hostname-override. value=\"\)[^\"]*\"" "\1$MACHINE_NAME\""


echo "Deployment name is ${DEPLOYMENT_NAME}"

# Ok, we are now ready to start the container.  
#Let's figure out whether we are logged in as admin or userX, and save it so we can get back to the right id later

tfile=$(mktemp /tmp/foo.XXXXXXXXX)
grid whoami > $tfile
cat $tfile | grep userX &> /dev/null
if [ $? -eq 0 ]; then
   	export AM_USERX='true'
	echo "Am userX, switching to admin"
multi_grid << eof
	logout --all
	login /users/xsede.org/admin --username=admin --password=admin
eof
else
   	cat $tfile | grep admin &> /dev/null
	if [ $? -eq 0 ]; then
   		export AM_ADMIN='true'
		echo "Am admin"
	else
		echo "Must be one of admin or userX to do this. Exiting. Have a nice day."
		exit
  	fi
fi
rm $tfile

exit

launch_container "$DEPLOYMENT_NAME"

#echo "Deployment name for backup is ${BACKUP_DEPLOYMENT_NAME}"
#launch_container "$BACKUP_DEPLOYMENT_NAME"

#restore_userdir

#get_root_privileges

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


