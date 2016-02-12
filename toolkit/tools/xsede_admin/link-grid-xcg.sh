#!/bin/bash

##############
#
# Author: Shava Smallen
#
# This script just calls link-grid-xcg.xml with the path to the epr file
##############

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

echo "GENII_INSTALL_DIR is set to $GENII_INSTALL_DIR"
whoami=`"$GENII_BINARY_DIR/grid" whoami | grep gffs-admins`
if [ $? -ne 0 ]; then
  echo "User must be member of gffs-admins group"
  exit 1
else 
  echo "Confirmed user is member of gffs-admins"
fi

"$GENII_BINARY_DIR/grid" script local:./link-grid-xcg.xml ./xcg-root-epr.xml

