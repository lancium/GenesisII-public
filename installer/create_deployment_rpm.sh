#!/bin/bash

# This script will create a genesis2 deployment package in RPM format.
# The user must already have created the deployment itself.

# fix this if the version is increased.
CURRENT_PACKAGE="genesis2-deployment-2.7"

depdir="$1"; shift

function print_instructions()
{
  echo "This script will create an RPM deployment package when given a directory"
  echo "that contains a valid deployment.  The directory passed on the command line"
  echo "should point at the top of the new deployment.  For example, if one's tree"
  echo "looks like:"
  echo
  echo "folder = /home/fred/my_deployments"
  echo "+--xcg3"
  echo "  +--configuration"
  echo "    |--cservices"
  echo "  +--security"
  echo "    |--default-owners"
  echo "    |--myproxy-certs"
  echo "    |--trusted-certificates"
  echo
  echo "Then the folder to pass to this script is:"
  echo "  /home/fred/my_deployments/xcg3"
  echo
}

if [ -z "$depdir" ]; then
  print_instructions
  exit 1
fi

if [ ! -d "$depdir" ]; then
  print_instructions
  echo
  echo "The directory specified does not seem to exist: '$depdir'"
  exit 1
fi

if [ ! -d "$depdir/security" ]; then
  print_instructions
  echo
  echo "The directory does not have a security folder: '$depdir'"
  echo "This is probably not a valid deployment."
  exit 1
fi

if [ ! -d "$depdir/configuration" ]; then
  print_instructions
  echo
  echo "The directory does not have a configuration folder: '$depdir'"
  echo "This is probably not a valid deployment."
  exit 1
fi

# create the package file that we will use.
depname="$(basename $depdir)"
echo "Deployment being packaged is called: $depname"
mkdir -p "$HOME/rpmbuild/SOURCES" 2>/dev/null
topdir="$HOME/rpmbuild/SOURCES/tempbuild"
rm -rf "$topdir" 2>/dev/null
stuffdir="$topdir/deployments"
mkdir -p "$stuffdir"
cp -R "$depdir" "$stuffdir"
pushd "$topdir"
if [ $? -ne 0 ]; then
  echo "There was a failure changing to the temporary dir: $topdir"
  exit 1
fi
sourcefile="$HOME/rpmbuild/SOURCES/genesis2_deployment.tar.gz"
tar -czf "$sourcefile" "deployments"
if [ $? -ne 0 ]; then
  echo "There was a failure creating the deployment archive in: $sourcefile"
  exit 1
fi
popd

#hmmm: we in right directory already? change to the workdir

rpmbuild --bb gen2deployment-2.7-1.spec 



