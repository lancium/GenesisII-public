#!/bin/bash

# this script does a 32-bit build of the genesisII software and creates the
# 32-bit installers with it, then it builds genesisII as 64-bit and creates
# those installers.
#
# Author: Chris Koeritz

##############

# boilerplate code to get our directories and tools figured out...

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../../prepare_tests.sh ../../prepare_tests.sh 
fi
export POSSIBLY_UNBUILT=true
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

# additional functions for managing installer.
source installer_helper.sh

##############

export INSTALLER_DIR="$GENII_INSTALL_DIR/installer"

##############

#hmmm: some churn...
export INSTALLER_NAME="genesis2.install4j"

export installer_config="$1"

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

# find the grid name that should be added to the installer binaries.
simple_name=$(sed -n -e 's/genii.simple-name=\(.*\)/\1/p' "$INSTALLER_DIR/$installer_config")
if [ -z "$simple_name" ]; then
  echo
  echo "Failed to find the grid's simple name in the config file."
  echo "There should be an entry called genii.simple-name that defines this."
  echo
  exit 1
fi
echo "Building installers for grid named: $simple_name"

# check for any stray keytabs that we absolutely do not want to include in
# the installer package.
if [ ! -z "$(find "$DEPLOYMENTS_ROOT/default" -iname "*keytab")" ]; then
  echo "There is a keytab file present under the path:"
  echo "    $DEPLOYMENTS_ROOT/default"
  echo "It is not safe to build an installer with private kerberos keytabs"
  echo "embedded in it."
  exit 1
fi

##############

OUTPUT_DIRECTORY=$HOME/installer_products
\rm -rf $OUTPUT_DIRECTORY
mkdir $OUTPUT_DIRECTORY

echo "Will build installers in $OUTPUT_DIRECTORY"

##############

# build 32-bit:

echo Building 32 bit Genesis...

pushd "$GENII_INSTALL_DIR"
ant clean
check_if_failed "ant clean failed"
replace_compiler_variables
check_if_failed "compiler variable replacement failed"
ant build
check_if_failed "ant build failed"
popd

build_installer 2078 "genesis2-gffs-linux-x86-32-${simple_name}"
build_installer 2084 "genesis2-gffs-windows-x86-32-${simple_name}"

##############

# build 64-bit:

echo Building 64 bit Genesis...

pushd "$GENII_INSTALL_DIR"
ant clean
check_if_failed "ant clean failed"
replace_compiler_variables
check_if_failed "compiler variable replacement failed"
ant -Dbuild.targetArch=64 build
check_if_failed "ant build failed"
popd

build_installer 5991 "genesis2-gffs-amd64-${simple_name}-deb"
#build_installer 5987 "genesis2-gffs-amd64-${simple_name}-rpm"
build_installer 3416 "genesis2-gffs-linux-amd64-${simple_name}"
build_installer 2088 "genesis2-gffs-macosx-amd64-${simple_name}"
build_installer 8974 "genesis2-gffs-windows-amd64-${simple_name}"

##############

fix_endings

generate_md5sums

# toss temporary files.
rm "$generated_installer_name"

echo Done building installers.

##############

