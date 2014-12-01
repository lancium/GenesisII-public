#!/bin/bash

# varying portion for platform.
export PLATFORM_ARCH=i586
export PLATFORM_BITSIZE=32
export BUILD_FLAGS='-Dbuild.targetArch=32'

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

installer_config="$1"; shift

export INSTALLER_DIR="$WORKDIR/.."

##############

if [ -z "$installer_config" \
    -o ! -f "$INSTALLER_DIR/$installer_config" ]; then
  echo
  echo A valid installer config file needs to be passed on the command line.
  echo
  echo The available choices are:
  pushd $INSTALLER_DIR &>/dev/null
  ls -1 *.config
  popd &>/dev/null
  echo
  exit 1
fi

CONFIGFILE="$INSTALLER_DIR/$installer_config"

# make sure our output folder is there.
if [ ! -d "$HOME/installer_products" ]; then
  mkdir "$HOME/installer_products"
fi

# set up a couple of installer files that the build doesn't know about.
cp ../current.version ../../current.version
if [ $? -ne 0 ]; then
  echo "failed to copy version file up for packaging."
  exit 1
fi
cp $CONFIGFILE ../../current.deployment
if [ $? -ne 0 ]; then
  echo "failed to copy deployment file up for packaging."
  exit 1
fi

# calculate some values from the config file.
export DEPLOYMENT_SOURCE_NAME=$(basename $(sed -n -e 's/genii.deployment-source=\(.*\)/\1/p' <$CONFIGFILE) )
export DEPLOYMENT_TARGET_NAME=$(sed -n -e 's/genii.new-deployment=\(.*\)/\1/p' <$CONFIGFILE)
export DEPLOYMENT_CONTEXT=$(sed -n -e 's/genii.deployment-context=\(.*\)/\1/p' <$CONFIGFILE)

if [ -z "$DEPLOYMENT_SOURCE_NAME" -o -z "$DEPLOYMENT_TARGET_NAME" -o -z "$DEPLOYMENT_CONTEXT" ]; then
  echo -e "one of the calculated variables was empty (DEPLOYMENT_SOURCE_NAME,\nDEPLOYMENT_TARGET_NAME, or DEPLOYMENT_CONTEXT).  there seems to be a problem\nwith the config file."
  exit 1
fi

echo dep source is $DEPLOYMENT_SOURCE_NAME
echo dep target is $DEPLOYMENT_TARGET_NAME
echo dep context is $DEPLOYMENT_CONTEXT

make
if [ $? -ne 0 ]; then
  echo "rpm installer build failed during make."
  exit 1
fi


