#!/bin/bash

# prints an error message (from parameters) and exits if the previous command failed.
function check_if_failed()
{
  if [ $? -ne 0 ]; then
    echo Step failed: $*
    exit 1
  fi
}

# prints out a timestamp with the current date and time up to seconds.
function date_string()
{
  date +"%Y_%b_%e_%H%M_%S" | sed -e 's/ //g'
}

# given a file name and a phrase to look for, this replaces all instances of
# it with a piece of replacement text.  note that slashes are okay in the two
# text pieces, but we are using pound signs as the regular expression
# separator; phrases including the octothorpe (#) will cause syntax errors.
function replace_phrase_in_file()
{
  file="$1"; shift
  phrase="$1"; shift
  replacement="$1"; shift
  if [ -z "$file" -o -z "$phrase" -o -z "$replacement" ]; then
    echo "replace_phrase_in_file: needs a filename, a phrase to replace, and the"
    echo "text to replace that phrase with."
    return 1
  fi
  sed -i -e "s%$phrase%$replacement%" "$file"
}


# returns 0 if there should be no problems using fuse, or non-zero if this platform
# does not currently support fuse.
function fuse_supported()
{
  local retval=0
  local platform="$(uname -a | tr A-Z a-z)"
  if [[ $platform =~ .*darwin.* ]]; then retval=1; fi
  if [[ $platform =~ .*cygwin.* ]]; then retval=1; fi
  if [[ $platform =~ .*ming.* ]]; then retval=1; fi
  return $retval
}

# returns 0 if there should be no problems creating links in the file system,
# or non-zero if this platform does not support symbolic links.
function links_supported()
{
  local retval=0
  local platform="$(uname -a | tr A-Z a-z)"
  if [[ $platform =~ .*cygwin.* ]]; then retval=1; fi
  if [[ $platform =~ .*ming.* ]]; then retval=1; fi
  return $retval
}

# Create a test directory "testDir" with $1 subdirectories, each with
# $2 subdirs, each with $3 files.
fan_out_directories()
{
  local top_count=$1; shift
  local mid_count=$1; shift
  local file_count=$1; shift
  mkdir testDir
  for (( di=0 ; di<$top_count ; di++ )); do
    mkdir testDir/sub$di
    for (( dj=0 ; dj<$mid_count ; dj++ )); do
      mkdir testDir/sub$di/sub$dj
      for (( dk=0 ; dk<$file_count ; dk++ )); do
        echo "file $di$dj$dk" > testDir/sub$di/sub$dj/file$di$dj$dk
      done
    done
  done
}

