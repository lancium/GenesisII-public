#!/bin/bash

##############
#
# Author: Shava Smallen
#
# This script just calls link-grid-xcg.xml with the path to the epr file
##############


export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z $GENII_INSTALL_DIR ]; then
  echo "export GENII_INSTALL_DIR before running the script."
  exit 1
fi
echo "GENII_INSTALL_DIR is set to $GENII_INSTALL_DIR"
whoami=`$GENII_INSTALL_DIR/grid whoami`
if [ $? -ne 0 ]; then
  echo "User must be member of gffs-admins group"
  exit 1
else 
  echo "Confirmed user is member of gffs-admins"
fi

$GENII_INSTALL_DIR/grid script local:./link-grid-xcg.xml ./xcg-root-epr.xml

