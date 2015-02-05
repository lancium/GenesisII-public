#!/bin/bash

# simple call to the md5sum generator.
#
# Author: Chris Koeritz

##############

# boilerplate code to get our directories and tools figured out...

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"
#export SHOWED_SETTINGS_ALREADY=true
#if [ -z "$XSEDE_TEST_SENTINEL" ]; then
#  source ../../prepare_tools.sh ../../prepare_tools.sh 
#fi
#export POSSIBLY_UNBUILT=true
#source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# additional functions for managing installer.
source $WORKDIR/../../xsede_tools/tools/installer/installer_helper.sh

##############

export INSTALLER_DIR="$GENII_INSTALL_DIR/installer"

##############

OUTPUT_DIRECTORY=$HOME/installer_products
if [ ! -d "$OUTPUT_DIRECTORY" ]; then
  mkdir "$OUTPUT_DIRECTORY"
fi


##############

pushd "$OUTPUT_DIRECTORY" &>/dev/null
generate_md5sums
popd &>/dev/null

echo "generated md5sums for installers."

##############

