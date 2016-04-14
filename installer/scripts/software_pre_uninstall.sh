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

# this script should run to clean up the install folder after the last install
# has gone.  currently only used for debian packages, which behave rationally.
# (rpms have really bizarre and wrong seeming behavior with this script, which
# seems to not work as either %preun or %postun, or the parameters that are
# claimed to be sent to the %preun script do not seem to show up.)

##############

# removing the generated container script should be safe.
\rm -f "$GENII_BINARY_DIR/GFFSContainer" &>/dev/null
# removing a couple of other generated files should work also.
\rm -f "$GENII_BINARY_DIR/gffschown" &>/dev/null
\rm -f "$GENII_BINARY_DIR/proxyio.launcher" &>/dev/null
\rm -f "$GENII_INSTALL_DIR/client-ui.desktop" &>/dev/null

exit 0
