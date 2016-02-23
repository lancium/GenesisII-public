#!/bin/bash

# This script allows a unified container configuration to be adapted to a
# different deployment than it previously used.  This usually needs to be a
# deployment that is compatible with the prior one, since moving a
# container's configuration between grids is not supported (not once the
# container holds some resources or has been linked to a particular grid).

##############

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# major variables for the script:

# the property file we will supply with container configuration.
export INSTALLER_FILE="$GENII_USER_DIR/installation.properties"

##############

# make sure we support using an altered deployment if that is configured.
if [ -z "$GENII_DEPLOYMENT_DIR" ]; then
  if [ -d "$GENII_USER_DIR/deployments" ]; then
    # use the personalized deployments folder in the state dir.
    export GENII_DEPLOYMENT_DIR="$GENII_USER_DIR/deployments"
  else
    # fall back to the default.
    export GENII_DEPLOYMENT_DIR="$GENII_INSTALL_DIR/deployments"
  fi
fi

##############

function print_instructions()
{
  echo "This script can update a Genesis II GFFS container to use a different"
  echo "or newer deployment.  Updating to a newer deployment for the same exact grid"
  echo "is the most common scenario.  If the deployment is for a totally different"
  echo "grid, then updating to the new deployment is only appropriate when the"
  echo "container has no existing configuration and no links within the old"
  echo "deployment's grid; this is typically only done at the start of a container's"
  echo "lifetime, if one is using a grid that is not available within an installer."
  echo "In both cases, the container is expected to already have the Unified"
  echo "Configuration type of container configuration."
  echo
  echo "The script requires that the GENII_INSTALL_DIR and GENII_USER_DIR are"
  echo "established as environment variables prior to updating the container."
  echo "Those variables should be set and made persistent for the user account, or"
  echo "there will be problems finding the right settings to run the container."
  echo "This can be accomplished by, for example, adding the variables to ~/.profile"
  echo "or ~/.bashrc like so (but using the actual path):"
  echo "   export GENII_INSTALL_DIR=/opt/genesis2-xsede"
  echo "For this script, the GENII_INSTALL_DIR should point at the newer"
  echo "installation that has been installed (either interactive or RPM/DEB)."
  echo
#this needs to change.  intuit the proper setting from the install dir's client.props?
#don't make the user fill this out.
  echo "The script requires the name of the deployment folder and the name of the"
  echo "grid's context file as parameters.  The deployment name must exist under the"
  echo "folder at '\$GENII_DEPLOYMENT_DIR' or under '\$GENII_USER_DIR/deployments',"
  echo "or in '\$GENII_INSTALL_DIR/deployments'.  It is okay if the deployment"
  echo "name or context file has not changed.  The context file must exist under"
  echo "the named deployment folder.  The script synchronizes the specified"
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

if [ -z "$GENII_USER_DIR" -o -z "$GENII_INSTALL_DIR" -o ! -d "$GENII_INSTALL_DIR" ]; then
  print_instructions
  echo
  if [ -z "$GENII_USER_DIR" ]; then
    echo
    echo "GENII_USER_DIR was not defined."
  fi
  if [ ! -d "$GENII_USER_DIR" ]; then
    echo
    echo "The GENII_USER_DIR does not exist yet!  There is no container configuration"
    echo "that can be updated currently.  Is this the right directory?:"
    echo "  $GENII_USER_DIR"
  fi
  if [ -z "$GENII_INSTALL_DIR" -o ! -d "$GENII_INSTALL_DIR" ]; then
    echo
    echo "GENII_INSTALL_DIR was not defined or does not exist."
  fi
  exit 1
fi

JAVA_PATH=$(which java)
if [ -z "$JAVA_PATH" ]; then
  print_instructions
  echo
  echo The GFFS container requires that Java be installed and be findable in the
  echo PATH.  The recommended JVM is the latest Java 8 available from Oracle.
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

# calculate the new deployment and new context.xml file from the install folder.
if [ ! -f "$GENII_INSTALL_DIR/lib/client.properties" ]; then
  print_instructions
  echo
  echo "There is no client.properties file to scrounge settings from in the"
  echo "GENII_INSTALL_DIR/lib directory.  This appears to not be a compatible install."
  exit 1
fi

new_deployment_info="$(grep "^edu.virginia.vcgr.genii.gridInitCommand=" "$GENII_INSTALL_DIR/lib/client.properties")"
if [ -z "$new_deployment_info" ]; then
  echo "There was a failure reading the deployment information from the installation"
  echo "directory.  The lib/client.properties file did not have the expected setting for"
  echo "'edu.virginia.vcgr.genii.gridInitCommand'."
  exit 1
fi

#echo "dep info for new install is: $new_deployment_info"

new_context_file="$(echo "$new_deployment_info" | sed -e "s/.*=\"local:\([^\"]*\)\".*/\1/")"
new_dep_dir="$(dirname "$new_context_file")"

#echo "new context file is: $new_context_file"
#echo "new dep dir is: $new_dep_dir"

# grab the current info.
old_deployment_info="$(grep "^edu.virginia.vcgr.genii.gridInitCommand=" "$INSTALLER_FILE")"
if [ -z "$old_deployment_info" ]; then
  echo "There was a failure reading the deployment information from the container"
  echo "state directory.  The installation properties file '$INSTALLER_FILE' did not"
  echo "have the expected setting for 'edu.virginia.vcgr.genii.gridInitCommand'."
  exit 1
fi
our_context_file="$(echo "$old_deployment_info" | sed -e "s/.*=\"local:\([^\"]*\)\".*/\1/")"
our_dep_dir="$(dirname "$our_context_file")"
our_deployment_name="$(echo "$old_deployment_info" | sed -e "s/.*\" \"\([^\"]*\)\".*/\1/")"

#echo "our context file is: $our_context_file"
#echo "our deployment directory is: $our_dep_dir"

if [ -z "$our_context_file" -o -z "$our_deployment_name" ]; then
  echo
  echo "Could not successfully calculate the old deployment configuration for"
  echo "the container."
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

# stop any running container to be sure we aren't changing config
# items while it's running.
echo "Stopping any existing container before configuration proceeds..."
tried_stopping=
if [ -f "$GENII_BINARY_DIR/GFFSContainer" ]; then
  "$GENII_BINARY_DIR/GFFSContainer" stop
  tried_stopping=true
fi

if [ ! -z "$tried_stopping" ]; then
  echo Waiting for container to completely stop.
  sleep 5
fi

##############

# load some of the variables from our config file.
#user_path="$(retrieve_compiler_variable genii.user-path)"

##############

# make the files available where we expect them.

# try to avoid copying to same file, which would indicate that the install
# is not using a specialized deployment and we're trying to copy in install dir.
if [ "$new_context_file" != "$our_context_file" ]; then
  cp "$new_context_file" "$our_context_file"
fi

# copy in the trusted certificates and other assets.
if [ "$new_dep_dir" != "$our_dep_dir" ]; then
  # make our target dirs just in case.  most will already exist.
  mkdir -p "$our_dep_dir/security/myproxy-certs" "$our_dep_dir/security/grid-certificates" "$our_dep_dir/security/trusted-certificates" &>/dev/null
  cp -f -r "$new_dep_dir/security/myproxy-certs"/* "$our_dep_dir/security/myproxy-certs"
  cp -f -r "$new_dep_dir/security/grid-certificates"/* "$our_dep_dir/security/grid-certificates"
  cp -f -r "$new_dep_dir/security/trusted-certificates"/* "$our_dep_dir/security/trusted-certificates"
  # we always overwrite the base trust store.
  cp -f "$new_dep_dir/security/trusted.pfx" "$our_dep_dir/security/trusted.pfx"
fi

# update the server config to the latest version.
cp -f "$GENII_INSTALL_DIR/webapps/axis/WEB-INF/server-config.wsdd" "$GENII_USER_DIR/webapps/axis/WEB-INF/server-config.wsdd" 

# write any updated config values we were given.

#echo Writing configuration to installer file: $INSTALLER_FILE

#replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.gridInitCommand=.*" "edu.virginia.vcgr.genii.gridInitCommand=\"local:$GENII_DEPLOYMENT_DIR/$our_dep/$context_file\" \"$our_dep\""

#replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$our_dep"

##############

# fix the default deployment if there's a deployments folder in state dir.
if [ -d "$GENII_USER_DIR/deployments" ]; then
  save_def="$(mktemp -d "$GENII_USER_DIR/deployments/old-default.XXXXXX")"
  mv "$GENII_USER_DIR/deployments/default" "$save_def"
  if [ $? -ne 0 ]; then
    echo "Moving the old deployment's default folder out of the way failed."
    exit 1
  fi
  cp -r "$GENII_INSTALL_DIR/deployments/default" "$GENII_USER_DIR/deployments/default"
  if [ $? -ne 0 ]; then
    echo "Copying newer default deployment into place failed."
    exit 1
  fi
  echo "Updated the state directory's default deployment from the new version."
fi

##############

# fix the java service wrapper to use the newest scheme.
replace_phrase_in_file "$GENII_USER_DIR/wrapper/wrapper.conf" "ext\/gffs-basics\.jar" "ext\/gffs-basics\*.jar"

##############

# get connected to the grid using the new deployment.
echo Connecting to the grid...
"$GENII_BINARY_DIR/grid" connect "local:$our_context_file" "$our_deployment_name"
if [ $? -ne 0 ]; then
  echo "Failed to connect to the grid!"
  echo "There may be more information in: ~/.GenesisII/grid-client.log"
  exit 1
fi
echo "Connection to grid succeeded."
echo

echo
echo "Done updating your container's deployment to the newest available."
echo
echo You can start the container service with:
echo "$GENII_BINARY_DIR/GFFSContainer start"
echo

