#!/bin/bash

# This script transforms a Genesis II container from a source-code-based
# installation into one based on the interactive installer.

##############

SOURCE_FOLDER="$1"; shift
INTERACTIVE_DIR="$1"; shift

if [ -z "$new_dep" -o -z "$context_file" ]; then
  print_instructions
  echo
  echo The new deployment name or the context file was not passed on the
  echo command line.
  exit 1
fi

##############

function print_instructions()
{
  echo -e "\
This script transforms a Genesis II container from a source-code-based\n\
installation into one based on the interactive installer.  It takes two\n\
parameters, (1) the source-code folder and (2) the interactive install folder.\n\
Before this script is executed, it is required that the interactive\n\
container installation program has already been run.\n\
"

  echo "For example:"
  echo
  local scriptname="$(basename $0)"
  echo "$scriptname $HOME/genesis-trunk $HOME/GenesisII"
  echo
}

##############

# validate the parameters we were given.

if [ -z "$SOURCE_FOLDER" -o -z "$INTERACTIVE_DIR" \
    -o ! -d "$SOURCE_FOLDER" -o ! -d "$INTERACTIVE_DIR" ]; then
  print_instructions
  echo
  echo "Both directory parameters are required and both must exist."
  exit 1
fi

# an extra check to make sure they're using the new installer in the 
# interactive folder.
if [ ! -f "$INTERACTIVE_DIR/current.version" \
    -o ! -f "$INTERACTIVE_DIR/current.deployment" ]; then
  print_instructions
  echo
  echo "It appears that the interactive installer folder provided is not pointing"
  echo "at a newer GFFS installation.  Please set this variable to the location where"
  echo "a 2.7.500+ installation is located."
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

# now copy over the folders involved.

# mktemp a new folder under interactive folder,
# mv interactive / deployments to the mktemp name,
# cp old folder deployments to the folder,
# but then move the old folder default deployments to a mktemp name also,
#  since that's out of date,
# and move back in the moved original version of defaults to that,

# copy over the container.properties file from old install.

# 

