#!/bin/bash

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source $WORKDIR/../../prepare_tests.sh $WORKDIR/../../prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
# otherwise load the rest of the tool environment.
source $XSEDE_TEST_ROOT/library/establish_environment.sh

####

function print_instructions()
{
  echo
  echo "This recopies a previously generated deployment back into the Genesis II"
  echo "installation folder.  This only makes sense if you have previously run the"
  echo "populate-deployment.sh script."
  exit 1
}

if [ -z "$NAMESPACE" ]; then
  print_instructions
  echo
  echo "*** No NAMESPACE variable was defined..."
  echo "The NAMESPACE environment variable must be set to either 'xsede' or 'xcg'."
  exit 1
fi

# we need this file to exist for us to redeploy.
DEP_INFO_FILE=saved_deployment_info.txt

if [ ! -f $DEP_INFO_FILE ]; then
  print_instructions
  echo
  echo "This does not seem to be a previously generated deployment folder.  The"
  echo "crucial file needed by this script is missing: $DEP_INFO_FILE"
  exit 1
fi

# pull in the variables we need.
source $DEP_INFO_FILE
if [ -z "$DEP_TYPE" -o -z "$DEP_NAME" -o -z "$PORT" -o -z "$MACHINE_NAME" ]; then
  print_instructions
  echo
  echo "The $DEP_INFO_FILE file does not seem properly formed.  It should define"
  echo "the following variables: DEP_TYPE, DEP_NAME, PORT, MACHINE_NAME"
  exit 1
fi

# load the functions for working with certificates.
source certificate-methods.sh

# now go generate some certificates for the ones that are missing.
generate_all_certificates "$MACHINE_NAME" "$DEP_NAME" "$DEP_TYPE" yes-redeploy

# now push all of the files into place.
populate_deployment

