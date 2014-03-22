#!/bin/bash

# Creates an archive from the test scripts in 3 flavors: GFFS, EMS, and both.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
# tell the test scripts not to require a GenesisII installation.
export POSSIBLY_UNBUILT=yes
source $XSEDE_TEST_ROOT/library/establish_environment.sh

pushd "$XSEDE_TEST_ROOT/.." &>/dev/null
justdir="$(basename $XSEDE_TEST_ROOT)"

date_string="$(date +"%Y_%b_%e_%H%M" | sed -e 's/ //g')"

EXCLUDES=(--exclude=".svn" --exclude="docs" --exclude="random*.dat" --exclude=gzip-1.2.4 --exclude=iozone3_397 --exclude="mount-*" --exclude="releases" --exclude="passwords.txt" --exclude="saved_deployment_info.txt" --exclude="generated_certs" --exclude="gridwide_certs" --exclude="inputfile.txt*")

tar -czf "$HOME/XSEDE_tools_${date_string}.tar.gz" "$justdir" ${EXCLUDES[*]} 

popd &>/dev/null

