#!/bin/bash

# lists processes that seem to be GenesisII java processes.  this takes one
# optional parameter that is a pattern to seek in the process info for the
# list candidates.  if the pattern is blank, all genesisII javas are shown,
# but if a pattern is provided, the process info must contain it.
#
# one example usage:
#   bash list_genesis_javas.sh backup
# should find any backup container.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
export POSSIBLY_UNBUILT=true
source $XSEDE_TEST_ROOT/library/establish_environment.sh

# see if they gave us a matching pattern.
export pattern="$1"; shift

find_genesis_javas "$pattern"

extra_flags=
if [ "$OS" == "Windows_NT" ]; then
  extra_flags="-W -p"
else
  extra_flags="wu --no-headers"
fi
#echo "got list: ${genesis_java_pids[*]}"
for i in ${genesis_java_pids[*]} ; do
  # can't really make these the same unfortunately, since cygwin ps just
  # doesn't cooperate for listing one process.
  line=
  if [ "$OS" == "Windows_NT" ]; then
    line="$i: $(ps $extra_flags $i | tail -n +2)"
  else
    line="$i: $(ps $extra_flags $i | awk '{print $11}')"
  fi
  # only print the first bit of the line.
  echo "${line:0:64}"
done 

