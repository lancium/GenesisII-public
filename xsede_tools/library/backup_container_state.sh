#!/bin/bash

# backs up the container state based on the GENII_USER_DIR and GENII_INSTALL_DIR variables.
#
# Author: Chris Koeritz

##############

function date_stringer() 
{ 
    local sep="$1"; shift
    if [ -z "$sep" ]; then sep='_'; fi
    date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

##############

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo
  echo "This script requires the genesis installation directory variable be"
  echo "set in the environment (GENIII_INSTALL_DIR)."
  exit 1
fi

if [ -z "$GENII_USER_DIR" ]; then
  export GENII_USER_DIR="$HOME/.genesisII-2.0"
  echo
  echo "Note: GENII_USER_DIR variable was not set; assuming that the default user"
  echo "directory is in use by this container ('$GENII_USER_DIR')."
  echo
fi

# check that the state dir actually exists.
if [ ! -d "$GENII_USER_DIR" ]; then
  echo
  echo "The GENII_USER_DIR variable points at a non-existent directory (currently"
  echo "set to '$GENII_USER_DIR')."
  echo "The script cannot continue."
  exit 1
fi

backup_file="$1"; shift

if [ -z "$backup_file" ]; then
  backup_file="$(\pwd)/gffs_state_backup_$(hostname)_${USER}_$(date_stringer).tar.gz"
else
  if [ "${backup_file:0:1}" != "/" ]; then
    # if it's not an absolute path, assume they mean wherever they are.
    backup_file="$(\pwd)/$backup_file"
  fi
fi

#hmmm: add auto shutdown and resume?
echo 
echo "** The container should currently be shut down! **"
echo 

if [ -z "$TMP" ]; then
  TMP="$HOME/.tmp"
fi
if [ ! -d "$TMP" ]; then
  mkdir "$TMP"
  if [ ! -d "$TMP" ]; then
    echo "There was a failure creating a local temporary directory at '$TMP'."
    exit 1
  fi
fi

# provide some extra files in case they're needed.
rm -rf "$GENII_USER_DIR/breadcrumbs"
mkdir "$GENII_USER_DIR/breadcrumbs"
# we will ignore any missing files, since some may not exist.
cp -R "$GENII_INSTALL_DIR/context.xml"* "$GENII_INSTALL_DIR/container.properties" "$GENII_INSTALL_DIR/xsede_tools"/*cfg "$GENII_INSTALL_DIR/deployments" "$GENII_INSTALL_DIR/xsede_tools/tools/deployment_generator" "$GENII_USER_DIR/breadcrumbs" 2>/dev/null

( pushd / ;
tar -czf $backup_file "$GENII_USER_DIR" ;
popd ) >$TMP/zz_container_backup_$(date_stringer).log

#hmmm: auto-restart?

echo "It is safe to start the container back up."

