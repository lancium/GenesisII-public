#!/bin/bash

# creates an archive of the deployment generator package, with the
# trusted-certs directory.  this does not include any of the trusted-certs
# key-pair PFX files, as they are not suitable for distribution.  it does
# generate a new TLS certificate for a container, so that people can
# immediately use the package to set up a new container that will interact
# with the grid properly.  if the deployment requires more certificate files
# or keypairs, they need to be added to the deployment package by the grid
# administrator or provided in some other secure manner to the user.

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source $WORKDIR/../../prepare_tests.sh $WORKDIR/../../prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
# otherwise load the rest of the tool environment.
source $XSEDE_TEST_ROOT/library/establish_environment.sh

####

if [ -z "$NAMESPACE" ]; then
  echo
  echo "*** No NAMESPACE variable was defined..."
  echo "The NAMESPACE environment variable must be set to either 'xsede' or 'xcg'."
  exit 1
fi

function date_stringer() 
{ 
  local sep="$1";
  shift;
  if [ -z "$sep" ]; then
    sep='_';
  fi;
  date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

# first we'll make up a nice new tls certificate.
bash create-one-cert.sh trusted_certs/tls-cert.pfx container Container

# next, we grab a copy of context.xml from the genesis2 folder.
# we want to include this in the package.
#if [ ! -f context.xml ]; then
  cp $GENII_INSTALL_DIR/context.xml .
  if [ $? -ne 0 ]; then
    echo Failed to copy the context.xml from $GENII_INSTALL_DIR
    exit 1
  fi
#fi

# now start packing up the deployment generator.
ARCHIVE_NAME=$HOME/deployment_pack_$(date_stringer).tar

pushd ..
# first we'll add the few key-pairs we let people have...
tar -cf $ARCHIVE_NAME deployment_generator/gridwide_certs/trusted.pfx deployment_generator/trusted_certs/tls-cert.*
if [ $? -ne 0 ]; then
  echo Failed to pack the trusted.pfx file.
  exit 1
fi
# get rid of the tls certificate; was only temporary for this package.
rm deployment_generator/trusted_certs/tls-cert.*

# then the rest of the deployment generator.
tar -rf $ARCHIVE_NAME deployment_generator --exclude=".svn" --exclude="*/generated_certs" --exclude="*/passwords.txt" --exclude="*/*.pfx" --exclude="*/saved_deployment_info.txt"
if [ $? -ne 0 ]; then
  echo Failed to pack the main portion of the deployment package.
  exit 1
fi
gzip $ARCHIVE_NAME
if [ $? -ne 0 ]; then
  echo Failed to compress the deployment package.
  exit 1
fi
popd


