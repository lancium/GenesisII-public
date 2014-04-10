#!/bin/bash

# useful functions that are somewhat general.  these are not needed for
# the basic setup of the test environment, but they are used by other
# test and tool functions and also by specific tests.
#
# Author: Chris Koeritz

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
  local file="$1"; shift
  local phrase="$1"; shift
  local replacement="$1"; shift
  if [ -z "$file" -o -z "$phrase" -o -z "$replacement" ]; then
    echo "replace_phrase_in_file: needs a filename, a phrase to replace, and the"
    echo "text to replace that phrase with."
    return 1
  fi
  sed -i -e "s%$phrase%$replacement%g" "$file"
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
##############

  # copied from open source codebase at: http://feistymeow.org
  # locates a process given a search pattern to match in the process list.
  function psfind() {
    local -a patterns=("${@}")
    mkdir $TEST_TEMP/grid_logs &>/dev/null
    local PID_DUMP="$(mktemp "$TEST_TEMP/grid_logs/zz_pidlist.XXXXXX")"
    local -a PIDS_SOUGHT
    if [ "$OS" == "Windows_NT" ]; then
      # needs to be a windows format filename for 'type' to work.
      if [ ! -d c:/tmp ]; then
        mkdir c:/tmp
      fi
      # windows7 magical mystery tour lets us create a file c:\\tmp_pids.txt, but then it's not
      # really there in the root of drive c: when we look for it later.  hoping to fix that
      # problem by using a subdir, which also might be magical thinking from windows perspective.
      tmppid=c:\\tmp\\pids.txt
      # we have abandoned all hope of relying on ps on windows.  instead we use wmic to get full
      # command lines for processes.
      wmic /locale:ms_409 PROCESS get processid,commandline </dev/null >"$tmppid"
      local flag='/c'
      if [ ! -z "$(uname -a | grep "^MING" )" ]; then
        flag='//c'
      fi
      # we 'type' the file to get rid of the unicode result from wmic.
      cmd $flag type "$tmppid" >$PID_DUMP
      \rm "$tmppid"
#      local CR='
#'  # embedded carriage return.
#      local appropriate_pattern="s/^.*  *\([0-9][0-9]*\)[ $CR]*\$/\1/p"
      local appropriate_pattern="s/^.*  *\([0-9][0-9]*\) *\$/\1/p"
      for i in "${patterns[@]}"; do
        PIDS_SOUGHT+=($(cat $PID_DUMP \
          | grep -i "$i" \
          | sed -n -e "$appropriate_pattern"))
      done
    else
      /bin/ps $extra_flags wux >$PID_DUMP
      # pattern to use for peeling off the process numbers.
      local appropriate_pattern='s/^[-a-zA-Z_0-9][-a-zA-Z_0-9]*  *\([0-9][0-9]*\).*$/\1/p'
      # remove the first line of the file, search for the pattern the
      # user wants to find, and just pluck the process ids out of the
      # results.
      for i in "${patterns[@]}"; do
        PIDS_SOUGHT+=($(cat $PID_DUMP \
          | sed -e '1d' \
          | grep -i "$i" \
          | sed -n -e "$appropriate_pattern"))
      done
    fi
    if [ ${#PIDS_SOUGHT[*]} -ne 0 ]; then
      local PIDS_SOUGHT2=$(printf -- '%s\n' ${PIDS_SOUGHT[@]} | sort | uniq)
      PIDS_SOUGHT=()
      PIDS_SOUGHT=${PIDS_SOUGHT2[*]}
      echo ${PIDS_SOUGHT[*]}
    fi
    /bin/rm $PID_DUMP
  }

#######

# tests the supposed fuse mount that is passed in as the first parameter.
function test_fuse_mount()
{
  local mount_point="$1"; shift
  local trunc_mount="$(basename "$(dirname $mount_point)")/$(basename "$mount_point")"

  checkMount="$(mount)"
#echo checkmount is: $checkMount
#echo mount point seeking is: $trunc_mount
  retval=1
  if [[ "$checkMount" =~ .*$trunc_mount* ]]; then retval=0; fi
  if [ $retval -ne 0 ]; then
    echo "Finding mount point '$trunc_mount' failed."
    return 1
  fi
  ls -l "$MOUNT_POINT" &>/dev/null
  return $?
}

#######

# also borrowed from feisty meow scripts...  by consent of author (chris koeritz).

  # switches from a /X/path form to an X:/ form.  this also processes cygwin paths.
  function unix_to_dos_path() {
    # we usually remove dos slashes in favor of forward slashes.
    if [ ! -z "$SERIOUS_SLASH_TREATMENT" ]; then
      # unless this flag is set, in which case we force dos slashes.
      echo "$1" | sed -e 's/\\/\//g' | sed -e 's/\/cygdrive//' | sed -e 's/\/\([a-zA-Z]\)\/\(.*\)/\1:\/\2/' | sed -e 's/\//\\/g'
    else
      echo "$1" | sed -e 's/\\/\//g' | sed -e 's/\/cygdrive//' | sed -e 's/\/\([a-zA-Z]\)\/\(.*\)/\1:\/\2/'
    fi
  }
  
  # switches from an X:/ form to an /X/path form.
  function dos_to_unix_path() {
    # we always remove dos slashes in favor of forward slashes.
    echo "$1" | sed -e 's/\\/\//g' | sed -e 's/\([a-zA-Z]\):\/\(.*\)/\/\1\/\2/'
  }

#######


