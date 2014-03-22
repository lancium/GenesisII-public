#!/bin/bash

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source $WORKDIR/../../prepare_tests.sh $WORKDIR/../../prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
# otherwise load the rest of the tool environment.
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# a shorthand for the deployment directory.
DEP_DIR="$GENII_INSTALL_DIR/deployments/$DEPLOYMENT_NAME"

####

function print_instructions()
{
  echo
  echo "This copies the necessary files to a Genesis II grid deployment."
  echo
  echo "It needs (1) the type of deployment (bootstrap, container, or client)."
  echo "The chosen option then takes the following extra parameters:"
  echo "For client, (2) is the short name of the deployment, and that's it."
  echo "For container and bootstrap, (2) is also the short name of the deployment,"
  echo "but then they also require (3) the port for the container to serve on,"
  echo "and (4) the external DNS name for the container."
  echo "Note that only a grid administrator should ever do the bootstrap deployment."
  echo
  echo "In addition, to support the different grid namespaces that are possible,"
  echo "the environment variable NAMESPACE should be defined.  The valid values"
  echo "for NAMESPACE are currently: 'xcg' or 'xsede'.  If you are bootstrapping"
  echo "the official xsede.org namespace, NAMESPACE should be 'xsede'."
  echo
  echo "Examples:"
  echo -e "\t$(basename $0) client xcg3"
  echo -e "\t$(basename $0) container xcg3 18443 poe.virginia.edu"
  echo -e "\t$(basename $0) bootstrap xcg3 18443 root.xcg.virginia.edu"
  exit 1
}

if [ -z "$NAMESPACE" ]; then
  echo "*** No NAMESPACE variable was defined..."
  echo
  print_instructions
fi

# load the functions for working with certificates.
source certificate-methods.sh

if [ $# -lt 2 ]; then print_instructions; fi

# grab the parameters before anything happens.
DEP_TYPE="$1"; shift
DEP_NAME="$1"; shift

if [ "$DEP_TYPE" != client ]; then
  # we should still have 2 parameters left.
  if [ $# -lt 2 ]; then print_instructions; fi
  PORT="$1"; shift
  MACHINE_NAME="$1"; shift
else
  # these settings are irrelevant for a client.
  PORT=8888
  MACHINE_NAME=bogus
fi

IS_BOOTSTRAP_CONTAINER="$DEP_TYPE"
if [ "$IS_BOOTSTRAP_CONTAINER" != "bootstrap" ]; then
  unset IS_BOOTSTRAP_CONTAINER
fi

# now go generate some certificates for the ones that are missing.
generate_all_certificates "$MACHINE_NAME" "$DEP_NAME" "$DEP_TYPE"
if [ $? -ne 0 ]; then
  echo "The step to generate certificates for the deployment has failed."
  exit 1
fi

# now push all of the files into place.
populate_deployment

