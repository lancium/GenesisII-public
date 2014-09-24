#!/bin/bash

# This script can create a deployment for the root container of a new grid.

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source "$WORKDIR/../../prepare_tools.sh" "$WORKDIR/../../prepare_tools.sh"

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi

# otherwise load the rest of the tool environment.
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# jump into the location where this script lives.
cd "$WORKDIR"

####

function print_instructions()
{
  echo
  echo "This script can populate a deployment folder for a bootstrapped container."
  echo
  echo "It needs these three parameters:"
  echo "(1) the short name of the deployment,"
  echo "(2) the port for the container to serve on, and"
  echo "(3) the external DNS name for the container."
  echo
  echo "In addition, to support the different grid namespaces that are possible,"
  echo "the environment variable FOLDERSPACE may be defined.  This should be the"
  echo "portion of the namespace that represents the grid.  For example, in the"
  echo "XSEDE grid, this is 'xsede.org'.  In the XCG grid, it is 'xcg.virginia.edu'."
  echo "The default for FOLDERSPACE is to use 'xsede.org'."
  echo
  echo "Example:"
  echo -e "\t$(basename $0) xsederoot 18443 gffs-1.xsede.org"
  echo
  exit 1
}

if [ -z "$FOLDERSPACE" ]; then
  export FOLDERSPACE="xsede.org"
fi

# load the functions for working with certificates.
source generator-methods.sh

if [ $# -lt 3 ]; then print_instructions; fi

# grab the parameters before anything happens.
DEP_NAME="$1"; shift
PORT="$1"; shift
MACHINE_NAME="$1"; shift

# now go generate some certificates for the ones that are missing.
generate_all_certificates "$MACHINE_NAME" "$DEP_NAME"
if [ $? -ne 0 ]; then
  echo "The step to generate certificates for the deployment has failed."
  exit 1
fi

# now push all of the files into place.
populate_deployment

