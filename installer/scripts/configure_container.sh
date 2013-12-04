#!/bin/bash

# This script configures a container for separate existence from a GenesisII
# installation.  In the past, the deployment would contain information about
# the container install, but now we can have multiple GenesisII installs per
# host.  Thus, the container information is now stored in a file in the state
# directory called installation.properties.  this file provides all the info
# required for the container to run properly without needing the installation
# directory to record its specific details.

##############

# major variables for the script:

# if this is non-empty, then we will generate certificates rather than expecting
# them to already exist.
GENERATE_CERTS=

# the property file we will supply with container configuration.
export INSTALLER_FILE="$GENII_USER_DIR/installation.properties"

# where we will hide the owner certificate for the container.
export LOCAL_CERTS_DIR="$GENII_USER_DIR/certs"

# storage for our specialized java service wrapper config file.
export WRAPPER_DIR="$GENII_USER_DIR/wrapper"

##############

# tls keystore defaults.

# where we expect the container to get its TLS cert now.
export LOCAL_TLS_CERT="$LOCAL_CERTS_DIR/tls-cert.pfx"

##############

# signing keystore item defaults.

SIGNING_KEYSTORE_FILE_PROPERTY="signing-cert.pfx"
SIGNING_KEY_PASSWORD_PROPERTY="container"
SIGNING_KEYSTORE_PASSWORD_PROPERTY="container"
SIGNING_KEY_ALIAS_PROPERTY=Container

##############

# the container's signing cert will be stored here now.
export LOCAL_SIGNING_CERT="$LOCAL_CERTS_DIR/$SIGNING_KEYSTORE_FILE_PROPERTY"

# make sure we support using an altered deployment if that is configured.
if [ -z "$GENII_DEPLOYMENT_DIR" ]; then
  export GENII_DEPLOYMENT_DIR="$GENII_INSTALL_DIR/deployments"
fi

##############

function print_instructions()
{
  echo "This script will configure a GFFS container and prepare it for use."
  echo "This depends on the GenesisII software already having been installed."
  echo "The script can also be used to reconfigure an existing container."
  echo
  echo "The script requires that the GENII_INSTALL_DIR and GENII_USER_DIR are"
  echo "established as environment variables prior to configuring a container."
  echo "Those variables should be set and made persistent for the user account, or"
  echo "there will be problems finding the right settings to run the container."
  echo "This can be accomplished by, for example, adding the variables to ~/.profile"
  echo "or ~/.bashrc like so:"
  echo "   export GENII_INSTALL_DIR=\$HOME/GenesisII"
  echo
  echo "This script uses six parameters to configure a container, and these need"
  echo "to be passed on the command line when invoking the script.  The parms are:"
  echo
  echo "(1) The container host name; this must be a globally resolvable host name"
  echo "    for the machine where the container is running.  If the host is not"
  echo "    globally visible then the container cannot be linked to the grid."
  echo
  echo "(2) The port number on which this container will be running.  This port"
  echo "    must be the exclusive property of the container, or there will be"
  echo "    network level conflicts.  This port must also be open on any firewalls"
  echo "    before the container can be linked into the grid."
  echo
  echo "(3) The grid user who will 'own' the container.  This must be an existing"
  echo "    user that the grid already knows.  XSEDE MyProxy and X509 identities"
  echo "    are both supported."
  echo
  echo "(4) A PFX file (in PKCS#12 format) for the container's TLS certificate"
  echo "    and private key.  This can be passed as 'generate' if the script should"
  echo "    generate the keystore rather than using an existing one."
  echo
  echo "(5) The keystore password for the container's TLS keypair.  The password"
  echo "    will allow the container to open up the keystore."
  echo
  echo "(6) An *optional* password for the TLS key within the keystore.  This can"
  echo "    be omitted if the key password is the same as the keystore password."
  echo "    This is not used for the 'generate' keyword as a PFX file (the generated"
  echo "    key will use the same keystore and key password)."
  echo
  echo "Examples:"
  echo
  local scriptname="$(basename $0)"
  echo "$scriptname corbomite.cs.virginia.edu 23013 jones generate \\"
  echo "  Falmouth18"
  echo
  echo "$scriptname fezzle.xsede.org 18843 ~/tlskey.pfx lincoln dqr891sb3 \\"
  echo "  grezne12"
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

# an extra check to make sure they're using the new installer as the GENII_INSTALL_DIR.
if [ ! -f "$GENII_INSTALL_DIR/current.version" \
    -o ! -f "$GENII_INSTALL_DIR/current.deployment" ]; then
  print_instructions
  echo
  echo "It appears that the GENII_INSTALL_DIR variable is not pointing at the"
  echo "newer installation.  Please set this variable to the location where the"
  echo "2.7.500+ installation is located."
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

# retrieve the parameters passed on the command line.
CONTAINER_HOSTNAME_PROPERTY="$1"; shift
CONTAINER_PORT_PROPERTY="$1"; shift
GRID_USER_NAME="$1"; shift
TLS_KEYSTORE_FILE_PROPERTY="$1"; shift
TLS_KEY_PASSWORD_PROPERTY="$1"; shift
TLS_KEYSTORE_PASSWORD_PROPERTY="$1"; shift

if [ -z "$CONTAINER_HOSTNAME_PROPERTY" -o \
  -z "$CONTAINER_PORT_PROPERTY" -o \
  -z "$TLS_KEY_PASSWORD_PROPERTY" -o \
  -z "$TLS_KEYSTORE_FILE_PROPERTY" -o \
  -z "$GRID_USER_NAME" ]; then
  print_instructions
  echo 
  echo "One of the required parameters is missing."
  exit 1
fi

if [ -z "$TLS_KEYSTORE_PASSWORD_PROPERTY" ]; then
  TLS_KEYSTORE_PASSWORD_PROPERTY="$TLS_KEY_PASSWORD_PROPERTY"
fi

if [ "$TLS_KEYSTORE_FILE_PROPERTY" == "generate" ]; then
  echo "Script will generate a TLS certificate for container."
  GENERATE_CERTS=true
  TLS_KEYSTORE_FILE_PROPERTY="$LOCAL_CERTS_DIR/tls-cert.pfx"
fi

if [ -z "$GENERATE_CERTS" -a ! -f "$TLS_KEYSTORE_FILE_PROPERTY" ]; then
  print_instructions
  echo 
  echo -e "The file specified for a TLS keypair cannot be found:\n$TLS_KEYSTORE_FILE_PROPERTY"
  exit 1
fi

##############

# setup the config directories.

if [ ! -d "$GENII_USER_DIR" ]; then
  mkdir "$GENII_USER_DIR"
fi
if [ ! -d "$LOCAL_CERTS_DIR" ]; then
  mkdir "$LOCAL_CERTS_DIR"
fi
if [ ! -d "$LOCAL_CERTS_DIR/default-owners" ]; then
  mkdir "$LOCAL_CERTS_DIR/default-owners"
fi

if [ ! -f "$INSTALLER_FILE" ]; then
  # the file doesn't exist yet.  we'll write a simple template for it.
  echo -e \
    "# This file provides GFFS container configuration properties.\n" \
    > "$INSTALLER_FILE"
  if [ $? -ne 0 ]; then
    echo "Writing a default installer file failed!  Target is: $INSTALLER_FILE"
    exit 1
  fi
fi

# stop any running container to be sure we aren't changing config
# items while it's running.
echo "Stopping any existing container before configuration proceeds..."
tried_stopping=
if [ -f "$GENII_INSTALL_DIR/GFFSContainer" ]; then
  "$GENII_INSTALL_DIR/GFFSContainer" stop
  tried_stopping=true
fi
if [ -f "$GENII_INSTALL_DIR/XCGContainer" ]; then
  "$GENII_INSTALL_DIR/XCGContainer" stop
  # clean up this older file.
  \rm "$GENII_INSTALL_DIR/XCGContainer"
  tried_stopping=true
fi
if [ ! -z "$tried_stopping" ]; then
  echo Waiting for container to completely stop.
  sleep 5
fi

if [ ! -d "$WRAPPER_DIR" ]; then
  mkdir "$WRAPPER_DIR"
fi

##############

# load some of the variables from our config file.
context_file="$(retrieve_compiler_variable genii.deployment-context)"
new_dep="$(retrieve_compiler_variable genii.new-deployment)"
user_path="$(retrieve_compiler_variable genii.user-path)"

##############

# write the config values we were given.
# we should have at least a blank installation property file now, but possibly
# one that is being reconfigured.  so let's replace the former values.

echo Writing configuration to installer file: $INSTALLER_FILE

# host and port.

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.external-hostname-override=.*" "edu.virginia.vcgr.genii.container.external-hostname-override=$CONTAINER_HOSTNAME_PROPERTY"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.listen-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$CONTAINER_PORT_PROPERTY"

# tls keystore info.

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.ssl.key-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-password=$TLS_KEY_PASSWORD_PROPERTY"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=$TLS_KEYSTORE_PASSWORD_PROPERTY"

# signing keystore info.

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.resource-identity.key-password=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-password=$SIGNING_KEY_PASSWORD_PROPERTY"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password=$SIGNING_KEYSTORE_PASSWORD_PROPERTY"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.resource-identity.container-alias=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.container-alias=$SIGNING_KEY_ALIAS_PROPERTY"

##############

# add in the values we provide defaults for; these can be overridden by people
# if needed, although we will slam defaults back in there if they run the
# configure container script again.

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.default-owners=.*" "edu.virginia.vcgr.genii.container.security.certs-dir=$LOCAL_CERTS_DIR"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.ssl.key-store=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store=$LOCAL_TLS_CERT"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.ssl.key-store-type=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store-type=PKCS12"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store=$LOCAL_SIGNING_CERT"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-type=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-type=PKCS12"

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.gridInitCommand=.*" "edu.virginia.vcgr.genii.gridInitCommand=\"local:$GENII_DEPLOYMENT_DIR/$new_dep/$context_file\" \"$new_dep\""

replace_if_exists_or_add "$INSTALLER_FILE" "edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$new_dep"

##############

# set up the service wrapper configuration.
if [ ! -f "$WRAPPER_DIR/wrapper.conf" ]; then
  cp "$GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/conf/wrapper.conf" "$WRAPPER_DIR/wrapper.conf"
fi

# we need to calculate the JRE directory for java, since our config file has
# "bin/java" in it already.
JRE_DIR="$( dirname $(dirname $JAVA_PATH) )"
replace_phrase_in_file "$WRAPPER_DIR/wrapper.conf" "\${installer:sys.preferredJre}" "$JRE_DIR"

# plug in a default amount of memory.  this can be adjusted manually if desired.
replace_phrase_in_file "$WRAPPER_DIR/wrapper.conf" "\${installer:genii.container-memory}" "2048"

# fix the logging directory for the service wrapper.
replace_phrase_in_file "$WRAPPER_DIR/wrapper.conf" "wrapper.logfile=.*" "wrapper.logfile=$HOME/.GenesisII/wrapper.log"

##############

# create a service-url file for this container.
echo "https://$CONTAINER_HOSTNAME_PROPERTY:$CONTAINER_PORT_PROPERTY/axis/services/VCGRContainerPortType" >"$GENII_USER_DIR/service-url.txt"

##############

# now do some heavy-weight operations where we actually use the gffs software.

# get connected to the grid.
echo Connecting to the grid...
"$GENII_INSTALL_DIR/grid" connect "local:$GENII_DEPLOYMENT_DIR/$new_dep/$context_file" "$new_dep"
if [ $? -ne 0 ]; then
  echo "Failed to connect to the grid!"
  echo "There may be more information in: ~/.GenesisII/grid-client.log"
  exit 1
fi

# download the owner's certificate.
echo "Downloading owner's certificate for user $GRID_USER_NAME..."
user_path=$(retrieve_compiler_variable genii.user-path)
"$GENII_INSTALL_DIR/grid" download-certificate "$user_path/$GRID_USER_NAME" "local:$LOCAL_CERTS_DIR/owner.cer"
if [ $? -ne 0 ]; then
  echo "Failed to download the certificate for grid user $GRID_USER_NAME."
  echo "There may be more information in: ~/.GenesisII/grid-client.log"
  exit 1
fi
cp "$LOCAL_CERTS_DIR/owner.cer" "$LOCAL_CERTS_DIR/default-owners"
if [ $? -ne 0 ]; then
  echo "Failed to copy the owner certificate into the default-owners folder."
  exit 1
fi

# generate a signing key for the container, but only if it is missing one.
if [ ! -f "$LOCAL_SIGNING_CERT" ]; then
  echo "Generating container's signing certificate..."
  generate_cert "$LOCAL_SIGNING_CERT" container
fi

# set up the TLS certificate.
if [ -z "$GENERATE_CERTS" ]; then
  # copy the file they specified into place.  we always do the copy, in case
  # the user is trying to reconfigure the TLS cert.
  echo "Copying specified TLS certificate for container..."
  cp -f "$TLS_KEYSTORE_FILE_PROPERTY" "$LOCAL_TLS_CERT"
  if [ $? -ne 0 ]; then
    echo "Failed to copy the specified TLS keypair into place!"
    echo "Tried copying $TLS_KEYSTORE_FILE_PROPERTY into $LOCAL_TLS_CERT"
    exit 1
  fi
else
  # generate a new tls certificate, but only if there is not one already.
  # we go on the assumption that one tls is as good as another, if the grid
  # generates it.  if the cert expired, then delete it before running the
  # configure container script and it will be regenerated.
  echo "Generating container's TLS certificate..."
  if [ ! -f "$LOCAL_TLS_CERT" ]; then
    generate_cert "$LOCAL_TLS_CERT" "$TLS_KEY_PASSWORD_PROPERTY"
  fi
fi

echo
echo Done configuring the container.
echo
echo The service URL for your container is stored in:
echo "$GENII_USER_DIR/service-url.txt"
echo
echo You can start the container service with:
echo "$GENII_INSTALL_DIR/GFFSContainer start"
echo

