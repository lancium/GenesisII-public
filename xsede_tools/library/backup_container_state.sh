#!/bin/bash

# backs up the container state based on the GENII_USER_DIR and GENII_INSTALL_DIR variables.
#
# Author: Chris Koeritz

if [ -z "$GENII_USER_DIR" -o -z "$GENII_INSTALL_DIR" ]; then
  echo "This script requires two variables in the environment, the user state"
  echo "directory (GENII_USER_DIR) and the genesis installation directory"
  echo "(GENIII_INSTALL_DIR)."
  exit 1
fi

#hmmm: add auto shutdown and resume?
echo 
echo "** The container should currently be shut down! **"
echo 

function date_stringer() 
{ 
    local sep="$1"; shift
    if [ -z "$sep" ]; then sep='_'; fi
    date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

backup_file="$(\pwd)/state_backup_$(date_stringer).zip"

if [ -z "$TMP" ]; then
  TMP=$HOME/.tmp
fi

( pushd / ;
zip -y -r $backup_file $GENII_USER_DIR $GENII_INSTALL_DIR/deployments $GENII_INSTALL_DIR/context.xml* $GENII_INSTALL_DIR/container.properties -x "*/.svn/*" -x "*/installer_base/*";
popd ) >$TMP/zz_container_backup_$(date_stringer).log

#hmmm: auto-restart

echo "It is safe to start the container back up."


