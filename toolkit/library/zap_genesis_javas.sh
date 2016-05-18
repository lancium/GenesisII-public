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

# check that it or they are really gone.
MAX_PROCESS_CHECKS=6
checking_count=0
while true; do
  genesis_java_pids=()
#echo empty list sees ${#genesis_java_pids[*]} genesis java processes
  find_genesis_javas "$pattern"
  checking_count=$(($checking_count + 1))
#echo checking count now at $checking_count
  for i in ${genesis_java_pids[@]} ; do
    # take a tiny snooze.  we do this first since checking immediately is counter-productive.
    sleep 1

    if [ $checking_count -gt $MAX_PROCESS_CHECKS ]; then
      echo "failed to zap java process $i normally; now zapping it decisively."
      if [ "$OS" == "Windows_NT" ]; then
#is there a more forceful version of this?
        taskkill -F -pid $i
      else
        kill -9 $i
      fi
    else
      echo "gffs java process $i is still running..."
    fi
  done

  if [ ${#genesis_java_pids[@]} -eq 0 ]; then
    echo "expected processes have all exited."
    break
#else
#echo still seeing ${#genesis_java_pids[*]} genesis java processes
  fi
done

# out of loop, we can leave now.

