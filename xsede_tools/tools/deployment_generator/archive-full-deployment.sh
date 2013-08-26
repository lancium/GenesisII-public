#!/bin/bash

# creates an archive of the deployment generator package with absolutely everything.
# this is intended for archival safe-keeping of a grid's deployment configuration only.
# it has all the keypairs for the grid.

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source $WORKDIR/../../prepare_tests.sh $WORKDIR/../../prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
# otherwise load the rest of the tool environment.
source $XSEDE_TEST_ROOT/library/establish_environment.sh

####

function date_stringer() 
{ 
  local sep="$1";
  shift;
  if [ -z "$sep" ]; then
    sep='_';
  fi;
  date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

ARCHIVE_NAME=$HOME/deployment_archive_with_keypairs_$(date_stringer).tar

pushd ..
tar -rf $ARCHIVE_NAME deployment_generator $GENII_INSTALL_DIR/context.xml --exclude=".svn"
if [ $? -ne 0 ]; then
  echo Failed to pack the deployment archive.
  exit 1
fi
gzip $ARCHIVE_NAME
if [ $? -ne 0 ]; then
  echo Failed to compress the deployment archive.
  exit 1
fi
popd


