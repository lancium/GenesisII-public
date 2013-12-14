#!/bin/bash

# This script can be used to fix the container configuration when the location
# for GenesisII has changed.

##############

function print_instructions()
{
  echo "This script can update the paths stored in the container's installation"
  echo "properties when the location of GenesisII has changed."
  echo
  echo "The script requires that the GENII_INSTALL_DIR and GENII_USER_DIR are"
  echo "established as environment variables prior to converting the container."
  echo "Those variables should be set and made persistent for the user account, or"
  echo "there will be problems finding the right settings to run the container."
  echo "This can be accomplished by, for example, adding the variables to ~/.profile"
  echo "or ~/.bashrc like so:"
  echo "   export GENII_INSTALL_DIR=\$HOME/GenesisII"
  echo
  echo "The script requires one parameter, which is the _old_ path where GenesisII"
  echo "was located.  It is assumed that GENII_INSTALL_DIR points at the new path."
  echo
  echo "For example, these commands will convert the paths for an older interactive"
  echo "install to the new RPM style installation:"
  echo
  local scriptname="$(basename $0)"
  echo "  export GENII_INSTALL_DIR=/opt/genesis2"
  echo "  bash $scriptname /home/fred/GenesisII"
}

##############

old_dir="$1"; shift

##############

# validate the parameters we were given.

if [ -z "$old_dir" ]; then
  print_instructions
  echo
  echo "The old GenesisII installation directory parameter was missing."
  exit 1
fi

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
  echo "The installation_helpers.sh script could not be located in the existing"
  echo "installation.  This is most likely because this install was created with"
  echo "the Genesisi v2.7.499 installer or earlier.  Please upgrade to the latest"
  echo "Genesis 2.7.500+ interactive installer before proceeding."
  exit 1
fi

source "$GENII_INSTALL_DIR/scripts/installation_helpers.sh"

##############

if [ ! -f "$GENII_USER_DIR/installation.properties" ]; then
  print_instructions
  echo
  echo "The installation.properties file does not exist.  This script can only operate"
  echo "on the self-contained type of container configuration."
  exit 1
fi

if [ ! -f "$GENII_USER_DIR/wrapper/wrapper.conf" ]; then
  print_instructions
  echo
  echo "The wrapper/wrapper.conf file does not exist.  This script can only operate"
  echo "on the self-contained type of container configuration."
  exit 1
fi

##############

replace_phrase_in_file "$GENII_USER_DIR/installation.properties" "$old_dir" "$GENII_INSTALL_DIR"
if [ $? -ne 0 ]; then
  echo "failed to replace directory in: $GENII_USER_DIR/installation.properties"
fi

replace_phrase_in_file "$GENII_USER_DIR/wrapper/wrapper.conf" "$old_dir" "$GENII_INSTALL_DIR"
if [ $? -ne 0 ]; then
  echo "failed to replace directory in: $GENII_USER_DIR/wrapper/wrapper.conf"
fi

replace_phrase_in_file "$GENII_USER_DIR/wrapper/wrapper.conf" ".*wrapper.java.command=.*" "#wrapper.java.command=NA"
if [ $? -ne 0 ]; then
  echo "failed to replace java command in: $GENII_USER_DIR/wrapper/wrapper.conf"
fi

##############

echo Updated the container configuration in: $GENII_USER_DIR

