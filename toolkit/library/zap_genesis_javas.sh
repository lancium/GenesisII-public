#!/bin/bash

# zaps processes that seem to be GenesisII java processes.  this takes one
# optional parameter that is a pattern to seek in the process info for the
# zap candidates.  if the pattern is blank, all genesisII javas are killed,
# but if a pattern is provided, the process info must contain it.
#
# one example usage:
#   bash zap_genesis_javas.sh backup
# should find any mirror container and whack it.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../prepare_tools.sh ../prepare_tools.sh 
fi
#export POSSIBLY_UNBUILT=true
#source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"
source "$GFFS_TOOLKIT_ROOT/library/helper_methods.sh"
source "$GFFS_TOOLKIT_ROOT/library/grid_management.sh"

# see if they gave us a matching pattern.
export pattern="$1"; shift

find_genesis_javas "$pattern"

for i in ${genesis_java_pids[*]} ; do
  echo zapping gII java process: $i
  if [ "$OS" == "Windows_NT" ]; then
    taskkill -F -pid $i
  else
    # we should not need a stronger type of signal than interrupt, or the
    # container is hosed up.
    kill $i
  fi
done 

#retval=0
## give a tiny snooze.
#sleep 2
#genesis_java_pids=()
#find_genesis_javas "$pattern"
#for i in ${genesis_java_pids[*]} ; do
#  echo FAILED to zap java process: $i
#  retval=1
#done
#exit $retval

