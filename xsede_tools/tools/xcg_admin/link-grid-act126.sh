#!/bin/bash

##############
#
# Author: Shava Smallen
#
# This script just calls link-grid-act126.xml with the path to the epr file
##############

eprfile="$1"

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z $GENII_INSTALL_DIR ]; then
  echo "export GENII_INSTALL_DIR before running the script."
  exit 1
fi
echo "GENII_INSTALL_DIR is set to $GENII_INSTALL_DIR"
#whoami=`$GENII_INSTALL_DIR/grid whoami | grep gffs-admins`
#if [ $? -ne 0 ]; then
#  echo "User must be member of gffs-admins group"
#  exit 1
#else 
#  echo "Confirmed user is member of gffs-admins"
#fi

if [ -z "$eprfile"  ]; then
  echo need epr filename
  exit 1
fi

$GENII_INSTALL_DIR/grid script local:./link-grid-act126.xml "$1"

