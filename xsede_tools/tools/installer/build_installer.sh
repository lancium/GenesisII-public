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
  source ../../prepare_tests.sh ../../prepare_tests.sh 
fi
export POSSIBLY_UNBUILT=true
source $XSEDE_TEST_ROOT/library/establish_environment.sh

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

# check for any stray keytabs that we absolutely do not want to include in
# the installer package.
if [ ! -z "$(find $DEPLOYMENTS_ROOT/default -iname "*keytab")" ]; then
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

# this creates a new installer file based on the chosen one that has the
# proper version number and such filled out.
function replace_compiler_variables()
{
  export generated_installer_name="$INSTALLER_DIR/generated-$INSTALLER_NAME"
  cp "$INSTALLER_DIR/$INSTALLER_NAME" "$generated_installer_name"
  check_if_failed "copying installer for $INSTALLER_NAME"

  # make sure we have this file available *inside* the install also.
  cp "$INSTALLER_DIR/$installer_config" "$GENII_INSTALL_DIR/current.deployment"
  # add version info.
  cp "$INSTALLER_DIR/current.version" "$GENII_INSTALL_DIR"

  local combo_file="$(mktemp /tmp/$USER-temp-instinfo.XXXXXX)"
  cat "$GENII_INSTALL_DIR/current.deployment" >>"$combo_file"
  cat "$GENII_INSTALL_DIR/current.version" >>"$combo_file"

  while read line; do
    if [ ${#line} -eq 0 ]; then continue; fi
    #echo got line to replace: $line
    # split the line into the variable name and value.
    IFS='=' read -a assignment <<< "$line"
    local var="${assignment[0]}"
    local value="${assignment[1]}"
    if [ "${value:0:1}" == '"' ]; then
      # assume the entry was in quotes and remove them.
      value="${value:1:$((${#value} - 2))}"
    fi
    #echo read var $var and value $value
    local seeking="$var\" value=\"[^\"]*\""
    local replacement="$var\" value=\"$value\""
    replace_phrase_in_file "$generated_installer_name" "$seeking" "$replacement"
  done < "$combo_file"

  echo "=============="
  echo "++ Configuration for this installer ++"
  cat "$combo_file" | sed -e '/^$/d'
  echo "=============="

  \rm -f "$combo_file"
}

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
  \rm -f $INSTALLER_DIR/Media/[a-zA-Z0-9]*

  pushd $INSTALLER_DIR &>/dev/null
  install4jc -b "$media_num" "$generated_installer_name"
  check_if_failed "building installer for $name_piece"
  for i in Media/*.dmg Media/*.sh Media/*.exe Media/*.rpm Media/*.deb; do
    if [ -f "$i" ]; then
      cp $i $OUTPUT_DIRECTORY
      check_if_failed "copying built installer for $name_piece to products"
    fi
  done
  cp Media/md5sums "$OUTPUT_DIRECTORY/md5sums.$name_piece"
  check_if_failed "copying md5sums for $name_piece to products"
  popd &>/dev/null

  # clean it out again so as not to leave cruft.
  \rm -f $INSTALLER_DIR/Media/*
}

function fix_endings()
{
  echo Fixing installer filename endings...
  pushd $OUTPUT_DIRECTORY &>/dev/null
  local i
  for i in *sh; do
    mv $i $(basename $i sh)script
  done
  for i in md5sums.*; do
    replace_phrase_in_file "$i" "\.sh$" "\.script"
  done
  popd &>/dev/null
}

##############

# build 32-bit:

echo Building 32 bit Genesis...

pushd $GENII_INSTALL_DIR
ant clean
check_if_failed "ant clean failed"
replace_compiler_variables
check_if_failed "compiler variable replacement failed"
ant build
check_if_failed "ant build failed"
popd

build_installer 2078 genesis2-gffs-linux32
build_installer 2084 genesis2-gffs-windows32

##############

# build 64-bit:

echo Building 64 bit Genesis...

pushd $GENII_INSTALL_DIR
ant clean
check_if_failed "ant clean failed"
replace_compiler_variables
check_if_failed "compiler variable replacement failed"
ant -Dbuild.targetArch=64 build
check_if_failed "ant build failed"
popd

build_installer 5991 genesis2-gffs-deb
build_installer 3416 genesis2-gffs-linux64
build_installer 2088 genesis2-gffs-mac64
build_installer 5987 genesis2-gffs-rpm

##############

fix_endings

# hide the evidence.
rm "$generated_installer_name"

echo Done building installers.

##############

