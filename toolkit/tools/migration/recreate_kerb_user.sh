#!/bin/bash

# this actually recreates a specifc type of kerberos user; one that
# authenticates against the xsede myproxy server.

# standard start-up boilerplate.
export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../../prepare_tools.sh ../../prepare_tools.sh 
fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

user="$1"; shift

if [ -z "$user" ]; then
  echo "This script requires an xsede portal user name.  An xsede compatible user will"
  echo "be created in the current bootstrapped grid."
  exit 1
fi

grid script local:"$GFFS_TOOLKIT_ROOT/tools/xsede_admin/create-xsede-user.xml" "$user"
grid chmod $SUBMIT_GROUP +rx "$USERS_LOC/$user"
grid ln $SUBMIT_GROUP "$USERS_LOC/$user/$(basename $SUBMIT_GROUP)"

