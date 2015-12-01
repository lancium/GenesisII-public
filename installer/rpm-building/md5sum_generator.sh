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

# grab installer directory off of command line.
export OUTPUT_DIR="$1"; shift

if [ -z "$OUTPUT_DIR" ]; then
  echo "$(basename $0): This script needs a directory where the installers exist that it can generate md5 sum files into."
  exit 1
fi

##############

pushd "$OUTPUT_DIR" &>/dev/null
generate_md5sums
popd &>/dev/null

echo "generated md5sums for installers."

##############

