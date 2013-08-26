#!/bin/bash

# Performs some setup to create the list of users specified in the input file and
# give them appropriate permissions to resources needed during the test.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

progname="$(basename $0)"

if [ $# -lt 1 ]; then
  echo "$progname: This script needs a single parameter, which is the container"
  echo "path to use for the authentication (e.g. $BOOTSTRAP_LOC)."
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

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.
}

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

#  if [ ! -z "$PASSWORD_PROVIDED" ]; then
    new_password=$PASSWORD_PROVIDED
#  elif [ -z "$NON_INTERACTIVE" ]; then
#    echo "Please input the password to use for $(basename $USERPATH)..."
#    read -s new_password
#    if [ -z "$new_password" ]; then
#      echo "The user's password cannot be empty.  Bailing out."
#      exit 1
#    else
#      echo "Password for '$USERPATH' given..."
#    fi
#  fi

  if [ -z "$new_password" ]; then
    echo "Cannot use an empty password for the new user.  Failing user creation."
    exit 1
  fi

  bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERPATH" "$new_password" "$SUBMIT_GROUP" "$HOMES_LOC" "$(dirname "$USERPATH")"
  assertEquals "Should create user on '$USERPATH' successfully" 0 $?
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
    bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$username" "$passwd" "$SUBMIT_GROUP" "$RNSPATH" "$(dirname "$username")"
    assertEquals "Should create user '$username' successfully" 0 $?
  done
}

# make sure we don't leave the user logged in as an administrator.
testLogoutAgain()
{
  grid logout --all
  assertEquals "Final logout of the grid" 0 $?
}

testLoginNormalUser()
{
  if [ -z "$NON_INTERACTIVE" ]; then
    echo "[$(date)]"
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

