#!/bin/bash

# This script allows a unified container configuration to be adapted to a
# different deployment than it previously used.  This usually needs to be a
# deployment that is compatible with the prior one, since moving a
# container's configuration between grids is not supported (once the
# container has some resources or has been linked to the old grid).

##############

# major variables for the script:

# the property file we will supply with container configuration.
export INSTALLER_FILE="$GENII_USER_DIR/installation.properties"

# where we will hide the owner certificate for the container.
export LOCAL_CERTS_DIR="$GENII_USER_DIR/certs"

##############

# make sure we support using an altered deployment if that is configured.
if [ -z "$GENII_DEPLOYMENT_DIR" ]; then
  export GENII_DEPLOYMENT_DIR="$GENII_INSTALL_DIR/deployments"
fi

##############

function print_instructions()
{
  echo "This script can update a Genesis II GFFS container to use a different"
  echo "or newer deployment.  Updating to a newer deployment for the same exact grid"
  echo "is the most common scenario; in this case, only the trust store and grid"
  echo "certificates are synchronized.  If the deployment is for a totally different"
  echo "grid, then updating to the new deployment is only appropriate when the"
  echo "container has no existing configuration and no links within the old"
  echo "deployment's grid; this is typically only done at the start of a container's"
  echo "lifetime, if one is using a grid that is not available within an installer."
  echo "In both cases, the container is expected to already have the Unified"
  echo "Configuration type of container configuration."
  echo
  echo "The script requires that the GENII_INSTALL_DIR and GENII_USER_DIR are"
  echo "established as environment variables prior to converting the container."
  echo "Those variables should be set and made persistent for the user account, or"
  echo "there will be problems finding the right settings to run the container."
  echo "This can be accomplished by, for example, adding the variables to ~/.profile"
  echo "or ~/.bashrc like so:"
  echo "   export GENII_INSTALL_DIR=\$HOME/GenesisII"
  echo "For this script, the GENII_INSTALL_DIR should point at the newer"
  echo "installation that has been installed (either interactive or RPM/DEB)."
  echo
  echo "The script requires the name of the deployment folder and the location of the"
  echo "grid's context file as parameters.  The deployment name must exist under the"
  echo "folder at '\$GENII_DEPLOYMENT_DIR' or (if that variable is not set) under the"
  echo "folder at '\$GENII_INSTALL_DIR/deployments'.  It is okay if the deployment"
  echo "name or context file has not changed.  The script synchronizes the specified"
  echo "deployment directory with the container's existing Unified Configuration."
  echo "For example:"
  echo
  local scriptname="$(basename $0)"
  echo "$scriptname current_grid xcg_context.xml"
  echo
  echo "Note that the context file is expected to reside under the named deployment."
}

##############

# validate the parameters we were given.

if [ -z "$GENII_USER_DIR" -o -z "$GENII_INSTALL_DIR" ]; then
  print_instructions
  echo
  if [ -z "$GENII_USER_DIR" ]; then
    echo "GENII_USER_DIR was not defined."
  fi
  if [ -z "$GENII_INSTALL_DIR" ]; then
    echo "GENII_INSTALL_DIR was not defined."
  fi
  exit 1
fi

if [ ! -f "$INSTALLER_FILE" ]; then
  echo "This script is intended to swap the deployment out for a container in"
  echo "the unified configuration format.  However, it appears that there is no"
  echo "existing unified container configuration; the installation.properties file"
  echo "needs to exist at:"
  echo "  $INSTALLER_FILE"
  echo "Please be sure that the GENII_USER_DIR is set appropriately and that the"
  echo "container already has a unified configuration."
  exit 1
fi

# an extra check to make sure they're using the new installer as the GENII_INSTALL_DIR.
if [ ! -f "$GENII_INSTALL_DIR/current.version" \
    -o ! -f "$GENII_INSTALL_DIR/current.deployment" ]; then
  print_instructions
  echo
  echo "It appears that the GENII_INSTALL_DIR variable is not pointing at a newer"
  echo "GFFS installation.  Please set this variable to the location where the"
  echo "2.7.500+ installation is located."
  exit 1
fi

new_dep="$1"; shift
context_file="$1"; shift

if [ -z "$new_dep" -o -z "$context_file" ]; then
  print_instructions
  echo
  echo The new deployment name or the context file was not passed on the
  echo command line.
  exit 1
fi

JAVA_PATH=$(which java)
if [ -z "$JAVA_PATH" ]; then
  print_instructions
  echo
  echo The GFFS container requires that Java be installed and be findable in the
  echo PATH.  The recommended JVM is the latest Java 7 available from Oracle.
  exit 1
fi

##############

# load our helper scripts.

if [ ! -f "$GENII_INSTALL_DIR/scripts/installation_helpers.sh" ]; then
  echo "The installation_helpers.sh script could not be located in the GenesisII"
  echo "installation, located in GENII_INSTALL_DIR, which is currently:"
  echo "  $GENII_INSTALL_DIR"
  echo "This is most likely because the current install was created with the"
  echo "Genesisi v2.7.499 installer or earlier.  Please upgrade to the latest"
  echo "Genesis 2.7.500+ interactive or RPM/DEB installer before proceeding."
  exit 1
fi

source "$GENII_INSTALL_DIR/scripts/installation_helpers.sh"

##############

# setup the config directories.

if [ ! -d "$GENII_USER_DIR" ]; then
  print_instructions
  echo
  echo "The GENII_USER_DIR does not exist yet!  There is no container configuration"
  echo "that can be updated currently.  Is this the right directory?:"
  echo "  $GENII_USER_DIR"
  exit 1
fi
if [ ! -d "$LOCAL_CERTS_DIR" ]; then
  print_instructions
  echo
  echo "The local certificates directory does not exist yet!  This is a bad"
  echo "state for the container configuration.  Is this the right directory?:"
  echo "  $LOCAL_CERTS_DIR"
  exit 1
fi

# stop any running container to be sure we aren't changing config
# items while it's running.
echo "Stopping any existing container before configuration proceeds..."
tried_stopping=
if [ -f "$GENII_INSTALL_DIR/GFFSContainer" ]; then
  "$GENII_INSTALL_DIR/GFFSContainer" stop
  tried_stopping=true
fi

if [ ! -z "$tried_stopping" ]; then
  echo Waiting for container to completely stop.
  sleep 5
fi

##############

# load some of the variables from our config file.
user_path="$(retrieve_compiler_variable genii.user-path)"

##############

# write any updated config values we were given.

echo Writing configuration to installer file: $INSTALLER_FILE

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.gridInitCommand=.*" "edu.virginia.vcgr.genii.gridInitCommand=\"local:$GENII_DEPLOYMENT_DIR/$new_dep/$context_file\" \"$new_dep\""

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$new_dep"

##############

# get connected to the grid using the new deployment.
echo Connecting to the grid...
"$GENII_INSTALL_DIR/grid" connect "local:$GENII_DEPLOYMENT_DIR/$new_dep/$context_file" "$new_dep"
if [ $? -ne 0 ]; then
  echo "Failed to connect to the grid!"
  echo "There may be more information in: ~/.GenesisII/grid-client.log"
  exit 1
fi

echo
echo "Done updating your container's deployment to the one specified."
echo
echo You can start the container service with:
echo "$GENII_INSTALL_DIR/GFFSContainer start"
echo

