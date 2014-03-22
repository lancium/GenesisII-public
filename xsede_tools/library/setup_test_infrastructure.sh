#!/bin/bash

# Performs some setup to create the list of users specified in the input file and
# give them appropriate permissions to resources needed during the test.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

progname="$(basename $0)"

if [ $# -lt 1 ]; then
  echo "$progname: This script needs a single parameter, which is the container"
  echo "path to use for the authentication (e.g. $STS_LOC)."
  echo "An optional second parameter can provide the password for the path"
  echo "'$USERPATH', but that is only required if the $USERPATH needs to be"
  echo "created as part of bootstrapping.  If it is blank, then no new user"
  echo "will be created."
  exit 3
fi

export ADMIN_CONTAINER="$1"; shift
echo "container location is $ADMIN_CONTAINER"
# override the variable in the input file, since we need to get this right.
export CONTAINERPATH="$ADMIN_CONTAINER"

# snag another parameter as a password, if they gave it.
if [ ! -z "$1" ]; then
  PASSWORD_PROVIDED="$1"; shift
else
  # they can ignore this password parameter, and should if the account already
  # exists.  we're going to assume that's what they're doing.
  PASSWORD_PROVIDED=""
  echo "No password provided, but it's only needed for bootstrapping."
fi

#oneTimeSetUp()
#{
#  sanity_test_and_init  # make sure test environment is good.
#}

# login the right power user that can create other user entries.
testLoginAsAdmin()
{
  if [ -z "$NON_INTERACTIVE" ]; then
    echo "[$(date)]"
    login_a_user admin
  fi
}

# make sure the main user listed in the input file exists.
testGetTestUserEstablished()
{
  if [ -z "$PASSWORD_PROVIDED" ]; then
    # no longer trying to do this step if the password is not passed.
    assertEquals "Only create '$USERPATH' if password provided" 0 0
    return 0
  fi

  new_password=$PASSWORD_PROVIDED

  if [ -z "$new_password" ]; then
    echo "Cannot use an empty password for the new user.  Failing user creation."
    exit 1
  fi

  bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERPATH" "$new_password" "$SUBMIT_GROUP" "$HOMES_LOC" "$(dirname "$USERPATH")"
  assertEquals "Create user at '$USERPATH'" 0 $?

  multi_grid <<eof
    mkdir --parents grid:$RNSPATH
    chmod "grid:$RNSPATH" +rwx "$USERPATH"
    onerror Failed to chmod $RNSPATH.
    chmod $BOOTSTRAP_LOC/Services/LightWeightExportPortType +rx $USERPATH
    chmod $BOOTSTRAP_LOC/Services/EnhancedRNSPortType +rx $USERPATH
    onerror Failed to give export capabilities to $USERPATH.
eof
  check_if_failed Could not give $USERPATH permission to the work area $RNSPATH
}

# now create a ton of demo users.
testCreateUsers()
{
  local x
  for (( x=0; x < ${#MULTI_USER_LIST[*]}; x++ )); do
    username="${MULTI_USER_LIST[$x]}"
    echo "Creating user '$username'..."
    passwd="${MULTI_PASSWORD_LIST[$x]}"
    # now do the heavy lifting to get that user set up.
    bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$username" "$passwd" "$SUBMIT_GROUP" "$HOMES_LOC" "$(dirname "$username")"
    assertEquals "Create user '$username'" 0 $?
    # also provide writability to the test user for staging job data.
    multi_grid <<eof
      chmod "grid:$RNSPATH" +rwx "$username"
      chmod "grid:$RNSPATH/.." +r "$username"
      chmod "grid:$RNSPATH/../.." +r "$username"
eof
    assertEquals "Allow '$username' to operate on '$RNSPATH'" 0 $?
  done
}

testLoginNormalUser()
{
  if [ -z "$NON_INTERACTIVE" ]; then
    echo "[$(date)]"
    grid logout --all
    login_a_user normal
  fi
}

oneTimeTearDown()
{
  echo "=========================================="
  echo "Finished setting up the test infrastructure."
  echo "The user '$USERPATH' has been created."
  echo "These demo users have been created: ${MULTI_USER_LIST[*]}"
  echo "=========================================="
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

