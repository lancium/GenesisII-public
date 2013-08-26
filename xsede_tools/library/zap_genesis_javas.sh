# Author: Chris Koeritz

# zaps processes that seem to be GenesisII java processes.  this takes one
# optional parameter that is a pattern to seek in the process info for the
# zap candidates.  if the pattern is blank, all genesisII javas are killed,
# but if a pattern is provided, the process info must contain it.
#
# one example usage:
#   bash zap_genesis_javas.sh backup
# should find any backup container and whack it.

#hmmm: currently also zaps install4j if it's running with genesis loaded up in it.

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

for i in ${genesis_java_pids[*]} ; do
  echo zapping gII java process: $i
  if [ "$OS" == "Windows_NT" ]; then
    taskkill -F -pid $i
  else
    kill $i
  fi
done 

