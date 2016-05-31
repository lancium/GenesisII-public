#!/bin/bash

echo "This script will add Globus Auth identities for all users in /users/xsede.org"
echo "who do not yet have them.  It will report any serious errors to the console."
echo "If a user already has a Globus Auth identity, then that user is skipped."

# cannot use fastgrid on centos currently.
#USE_FASTGRID=y

if [ ! -z "$USE_FASTGRID" ]; then
  # quit to shake any prior credentials this client could remember.
  fastgrid -q
  GRIDTOOL=fastgrid
else
  GRIDTOOL=grid
fi

$GRIDTOOL cd /users/xsede.org
if [ $? -ne 0 ]; then
  echo "Failed to change directory to /users/xsede.org.  Is the grid command"
  echo "available?  Are you logged in as a grid administrator?"
  exit 1
fi

CONTAINER="/resources/xsede.org/containers/sts-1.xsede.org"

TEMP_USER_LIST="$(mktemp /tmp/globusauthanizer-users.XXXXXX)"
CURRENT_OUTPUT="$(mktemp /tmp/globusauthanizer-run.XXXXXX)"

$GRIDTOOL ls | tail -n +2 >"$TEMP_USER_LIST"

while read line ; do
  # blank line for readability, per user attempt.
  echo
  if [ ! -z "$line" ]; then
    bash $GFFS_TOOLKIT_ROOT/tools/xsede_admin/create-globusauth-user.sh $CONTAINER $line &>"$CURRENT_OUTPUT"
    retval=$?
    if [ $retval -eq 8 ]; then
      echo "SKIP: $line already has a Globus Auth identity."
    elif [ $retval -eq 9 ]; then
      echo "SKIP: $line is not a Kerberos type identity so skipping."
    elif [ $retval -ne 0 ]; then
      echo "FAIL: There was a failure adding a Globus Auth identity to: $line"
      echo "Are you logged in with administrative credentials?"
      local_file="$HOME/globusauth-failure-$line"
      cp "$CURRENT_OUTPUT" "$local_file"
      echo "Failure log recorded in: $local_file"
    else
      echo "OKAY: $line was successfully given a Globus Auth identity."
    fi
    # clean out the temp file.
    \rm -f "$CURRENT_OUTPUT"
  fi
done < "$TEMP_USER_LIST"

# clean out the users list file too.
\rm -f "$TEMP_USER_LIST"

echo "Done adding Globus Auth STS for existing users."

