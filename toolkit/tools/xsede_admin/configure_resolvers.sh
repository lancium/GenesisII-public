#!/bin/bash

# this script sets up resolver permissions for the xsede groups.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

#if [ -z "$GENII_INSTALL_DIR" ]; then
#  echo This script needs the GENII_INSTALL_DIR variable to be set ahead of time.
#  echo It also requires administrative permissions to run.
#  exit 1
#fi

present=$("$GENII_BINARY_DIR/grid" ls /etc/resolvers | grep rootResolver)
if [ ! -z "$present" ]; then
  echo Setting permissions on rootResolver.
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/rootResolver +w /groups/xsede.org/gffs-admins
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/rootResolver +w /groups/xsede.org/gffs-amie
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/rootResolver +rx --everyone
else
  echo There is currently no rootResolver to configure.
  # this may be expected.  in any case, we definitely expect there to be no
  # sts resolver if there is no root resolver.
  exit 0
fi

present=$("$GENII_BINARY_DIR/grid" ls /etc/resolvers | grep stsResolver)
if [ ! -z "$present" ]; then
  echo Setting permissions on stsResolver.
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/stsResolver +w /groups/xsede.org/gffs-admins
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/stsResolver +w /groups/xsede.org/gffs-amie
  "$GENII_BINARY_DIR/grid" chmod /etc/resolvers/stsResolver +rx --everyone
else
  echo There is currently no stsResolver to configure.
  exit 0
fi

# if other resolvers get added, they would go here...

exit 0

