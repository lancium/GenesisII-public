#!/bin/bash

# Runs doxygen to build a set of documentation.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

OUTPUT_FOLDER=$HOME/docs_xsede
if [ ! -d $OUTPUT_FOLDER ]; then
  mkdir $OUTPUT_FOLDER  
fi

pushd $OUTPUT_FOLDER
doxygen "$WORKDIR"/*.config
popd

