#!/bin/bash

# restores the container state from a prior backup file.
#
# Author: Chris Koeritz

if [ -z "$GENII_USER_DIR" -o -z "$GENII_INSTALL_DIR" ]; then
  echo "This script requires two variables in the environment, the user state"
  echo "directory (GENII_USER_DIR) and the genesis installation directory"
  echo "(GENIII_INSTALL_DIR)."
  exit 1
fi

backup_file=$1; shift

if [ -z "$backup_file" ]; then
  echo "This script requires a parameter that specifies the backup file"
  echo "to restore grid state from."
  exit 1
fi

# makes sure that, if the backup file is a local path, that we can still find it
# after changing directories.
pushd / >/dev/null
if [ ! -f "$backup_file" ]; then
  echo "This script cannot find the backup file called:"
  echo "  '$backup_file'"
  echo "Please use the absolute path to the file."
  exit 1
fi
popd >/dev/null

function date_stringer() 
{ 
    local sep="$1"; shift
    if [ -z "$sep" ]; then sep='_'; fi
    date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

#hmmm: add auto shutdown and resume?
echo 
echo "** The container should currently be shut down! **"
echo 

echo Whacking the current GENII_USER_DIR: $GENII_USER_DIR
\rm -rf "$GENII_USER_DIR"

if [ -e "$GENII_USER_DIR" ]; then
  echo The genesis state directory is still present in: $GENII_USER_DIR
  echo Please delete it manually since there is some issue with permissions.
  exit 1
fi

if [ -z "$TMP" ]; then
  TMP=$HOME/.tmp
fi

#hmmm: will this always work?  maybe not if the state or install dir isn't under home?
#      if it stores absolute paths, then it would still work.
( pushd /;
unzip -o $backup_file ;
popd ) >>$TMP/zz_container_backup_$(date_stringer).log

#hmmm: auto-restart

echo "It is safe to start the container back up."


