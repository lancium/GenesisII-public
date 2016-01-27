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
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../../prepare_tools.sh ../../prepare_tools.sh 
fi
export POSSIBLY_UNBUILT=true
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# additional functions for managing installer.
source installer_helper.sh

##############

export INSTALLER_DIR="$GENII_INSTALL_DIR/installer"

##############

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

OUTPUT_DIRECTORY="$HOME/installer_products-${simple_name}"
if [ ! -d "$OUTPUT_DIRECTORY" ]; then
  mkdir "$OUTPUT_DIRECTORY"
fi

echo "Will build installers in $OUTPUT_DIRECTORY"

##############

# build the code:

echo Building Genesis...

pushd "$GENII_INSTALL_DIR"
#ant clean
#check_if_failed "ant clean failed"
replace_compiler_variables
check_if_failed "compiler variable replacement failed"
ant -Dbuild.targetArch=64 build
check_if_failed "ant build failed"
popd

build_installer 3416 "genesis2-gffs-linux-amd64-${simple_name}"
build_installer 11295 "genesis2-gffs-linux-x86-32-${simple_name}"

build_installer 2088 "genesis2-gffs-macosx-amd64-${simple_name}"

build_installer 5991 "genesis2-gffs-amd64-${simple_name}-deb"

build_installer 8974 "genesis2-gffs-windows-amd64-${simple_name}"
build_installer 11297 "genesis2-gffs-windows-x86-32-${simple_name}"

#build_installer 8972 "genesis2-gffs-linux-powerpc-${simple_name}"

##############

# get the version file up there for reference.
cp "$GENII_INSTALL_DIR/current.version" "$OUTPUT_DIRECTORY"
cp "$GENII_INSTALL_DIR/current.deployment" "$OUTPUT_DIRECTORY"

fix_endings

generate_md5sums

chmod 755 "$OUTPUT_DIRECTORY"/*.deb "$OUTPUT_DIRECTORY"/*.script "$OUTPUT_DIRECTORY"/*.rpm "$OUTPUT_DIRECTORY"/*.dmg "$OUTPUT_DIRECTORY"/*.exe 

# toss temporary files.
rm "$generated_installer_name"

echo Done building installers.

##############

