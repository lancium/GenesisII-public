#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

if [ -z "$I4J_INSTALL_LOCATION" ]; then
  # get the directory up a level from script location.
  export GENII_INSTALL_DIR="$(cd "$WORKDIR/.." && \pwd )"
else
  # use the install4j location since we know it.
  export GENII_INSTALL_DIR="$I4J_INSTALL_LOCATION"
fi

# force our script to run with bash.
exec /bin/bash $GENII_INSTALL_DIR/scripts/main_installation_script.sh $GENII_INSTALL_DIR
