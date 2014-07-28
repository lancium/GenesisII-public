#!/bin/bash

# helper methods for working with installers.
#
# Author: Chris Koeritz

#hmmm: in progress, note that this has not been separated completely from the caller yet;
#  the installer_config variable is still needed, probably others.
#  plus the replace_compiler_variables name is not a great name.

##############

# the following functions require:
#   GENII_INSTALL_DIR
#   OUTPUT_DIRECTORY

# this creates a new installer file based on the chosen one that has the
# proper version number and such filled out.
function replace_compiler_variables()
{
  export INSTALLER_DIR="$GENII_INSTALL_DIR/installer"

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
#  cp Media/md5sums "$OUTPUT_DIRECTORY/md5sums.$name_piece"
#  check_if_failed "copying md5sums for $name_piece to products"
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
  popd &>/dev/null
}

# make md5sums for all files we see in the output dir so users can
# verify the installers.
function generate_md5sums()
{
  rm -f $OUTPUT_DIRECTORY/md5sums
  md5sum $OUTPUT_DIRECTORY/* >$OUTPUT_DIRECTORY/md5sums
}


