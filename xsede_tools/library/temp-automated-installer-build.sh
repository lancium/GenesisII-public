#!/bin/bash

# this script does a 32-bit build of the genesisII software and creates the
# 32-bit installers with it, then it builds genesisII as 64-bit and creates
# those installers.
#
# Author: Chris Koeritz

##############

# boilerplate code to get our directories and tools figured out...

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
export POSSIBLY_UNBUILT=true
source $XSEDE_TEST_ROOT/library/establish_environment.sh

##############

export installer_name="$1"

if [ -z "$installer_name" \
    -o ! -f "$GENII_INSTALL_DIR/installer/install4j/$installer_name" ]; then
  echo
  echo A valid installer file name needs to be passed on the command line.
  echo The first parameter should be the basename of the install4j file.
  echo
  echo The available choices so far are:
  pushd $GENII_INSTALL_DIR/installer/install4j &>/dev/null
  ls -1 *.install4j
  popd &>/dev/null
  echo
  exit 1
fi

# check for any stray keytabs that we absolutely do not want to include in
# the installer package.
if [ ! -z "$(find $GENII_INSTALL_DIR/deployments/default -iname "*keytab")" ]; then
  echo "There is a keytab file present under the path:"
  echo "    $GENII_INSTALL_DIR/deployments/default"
  echo "It is not safe to build an installer with private kerberos keytabs"
  echo "embedded in it."
  exit 1
fi

##############

OUTPUT_DIRECTORY=$GENII_INSTALL_DIR/installer/products
\rm -rf $OUTPUT_DIRECTORY
mkdir $OUTPUT_DIRECTORY

echo "Will build installers in $OUTPUT_DIRECTORY"

##############

# creates the installer designated by the media number passed in.
# this also needs a short name for the installer being built.
function build_installer()
{
  local media_num=$1; shift
  local name_piece="$1"; shift

  if [ -z "$media_num" -o -z "$name_piece" ]; then
    echo This function requires the media number for the media file to build and
    echo a portion of the name to use for describing that installer.
  fi

  # clean out the media folder.
  \rm -f $GENII_INSTALL_DIR/installer/install4j/Media/*

  pushd $GENII_INSTALL_DIR/installer/install4j &>/dev/null
  install4jc -b "$media_num" "$installer_name"
  check_if_failed "building installer for $name_piece"
  for i in Media/*.dmg Media/*.sh Media/*.exe; do
    if [ -f "$i" ]; then
      cp $i $OUTPUT_DIRECTORY
      check_if_failed "copying built installer for $name_piece to products"
    fi
  done
  cp Media/md5sums "$OUTPUT_DIRECTORY/md5sums.$name_piece"
  check_if_failed "copying md5sums for $name_piece to products"
  popd &>/dev/null

  # clean it out again so as not to leave cruft.
  \rm -f $GENII_INSTALL_DIR/installer/install4j/Media/*
}

##############

# first build 32-bit:

#echo Building 32 bit Genesis...
#pushd $GENII_INSTALL_DIR
#ant clean
#ant update
#ant build
#popd
#
#build_installer 3414 genesis2_linux_client_32
#build_installer 2078 genesis2_linux_container_32
#build_installer 2081 genesis2_windows_client_32
#build_installer 2084 genesis2_windows_container_32

##############

# then build 64-bit:

echo Building 64 bit Genesis...

#pushd $GENII_INSTALL_DIR
#ant clean
#ant update
#ant -Dbuild.targetArch=64 build
#popd

build_installer 2073 genesis2_linux_client_64
build_installer 3416 genesis2_linux_container_64
build_installer 2088 genesis2_mac_client_64

##############

echo Fixing installer filename endings...

pushd $OUTPUT_DIRECTORY &>/dev/null
for i in *sh; do
  mv $i $(basename $i sh)bin
done
popd &>/dev/null

echo Done building installers.

