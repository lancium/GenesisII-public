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
source "$WORKDIR/../../prepare_tools.sh" "$WORKDIR/../../prepare_tools.sh"

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi

# otherwise load the rest of the tool environment.
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

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

####

# jump to where this script lives.
cd "$WORKDIR"

# must be in synch with generator methods value.
DEPLOYMENT_MEMORY_FILE=saved-deployment-info.txt

if [ ! -f $DEPLOYMENT_MEMORY_FILE ]; then
  echo "This does not appear to be a valid deployment folder, because the file"
  echo "'saved-deployment-info.txt' is missing."
  exit 1
fi

# get the variables we need to know about the deployment.
source $DEPLOYMENT_MEMORY_FILE

source generator-methods.sh

# point a variable at the new deployment.
DEP_DIR="$GENII_INSTALL_DIR/deployments/$DEP_NAME"

export OUTDIR=deployment-info

# create our storage directory.
rm -rf $OUTDIR
check_if_failed "cleaning old $OUTDIR folder"
mkdir $OUTDIR
check_if_failed "making new $OUTDIR folder"
cp -r "$DEP_DIR/security" $OUTDIR
check_if_failed "copying security from new deployment"
mkdir $OUTDIR/configuration
check_if_failed "creating config directory in output folder"
cp "$DEP_DIR/configuration/myproxy.properties" $OUTDIR/configuration
check_if_failed "copying myproxy.properties from new deployment"
cp "$DEP_DIR/configuration/namespace.properties" $OUTDIR/configuration
check_if_failed "copying namespace.properties from new deployment"

# grab a copy of context.xml from the genesis2 folder.
cp "$GENII_INSTALL_DIR/context.xml" $OUTDIR
if [ $? -ne 0 ]; then
  echo Failed to copy the context.xml from $GENII_INSTALL_DIR
  exit 1
fi

# start packing up the deployment generator files.
ARCHIVE_NAME=$HOME/deployment_pack_$(date_stringer).tar.gz

####

tar -czf $ARCHIVE_NAME $OUTDIR --exclude=".svn" --exclude="passwords.txt" --exclude="bootstrap*xml" --exclude="admin.pfx" --exclude="tls-cert.pfx" --exclude="signing-cert*pfx"
if [ $? -ne 0 ]; then
  echo Failed to pack up the deployment package.
  exit 1
fi

echo "Created deployment package for deployment '$DEP_NAME' in file:"
echo $ARCHIVE_NAME

rm -rf $OUTDIR

