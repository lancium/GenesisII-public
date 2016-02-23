#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

if [ -z "$I4J_INSTALL_LOCATION" ]; then
  # get the directory up a level from script location.
  export GENII_INSTALL_DIR="$(cd "$WORKDIR/.." && \pwd )"
else
  # use the install4j location since we know it.
  export GENII_INSTALL_DIR="$I4J_INSTALL_LOCATION"
fi
export GENII_BINARY_DIR="$GENII_INSTALL_DIR/bin"

##############

#hmmm: this is still somehow not safe.  if we remove here, the new link somehow does not show up.
# removing the generated container script should be safe.
#\rm -f "$GENII_INSTALL_DIR/bin/GFFSContainer" &>/dev/null
# if that was the last thing, we can remove the dir too.
#rmdir "$GENII_INSTALL_DIR/bin" &>/dev/null

exit 0
