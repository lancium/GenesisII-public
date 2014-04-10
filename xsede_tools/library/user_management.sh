#!/bin/bash

# User management functions for the grid.
#
# Author: Chris Koeritz

# global variables:
#
#   OLD_STATE_DIR: this holds onto the prior version of GENII_USER_DIR.
#   OLD_QUEUE_PATH: remembers the prior setting for the QUEUE_PATH.

DIR_KEYWORD="multi-state"

# establishes a separate user identity with its own state directory.
# this changes the variable that points at the state directory!
# it needs two parameters: the user and the password.
# it can optionally take a third for the IDP path used to authenticate.
put_on_hat()
{
  if [ $# -lt 2 ]; then
    echo To put on the hat of a user, we need two parameters: user and password
    return 2
  fi
  user="$1"; shift
  password="$1"; shift
  idp_path="$1"; shift
  OLD_STATE_DIR="$GENII_USER_DIR"
  # we have a simple root area for these states.
  new_dir="$TEST_TEMP/$DIR_KEYWORD"
  if [ ! -d "$new_dir" ]; then mkdir $new_dir; fi
  if [ ! -d "$new_dir" ]; then
    echo "Failed to create our root multi-state directory in $new_dir"
    return 2
  fi
  # pick a state dir for the user that's under our state area.
  export GENII_USER_DIR="$new_dir/state-$RANDOM-$user"
  # let's hope we got this path right...
  rm -rf "$GENII_USER_DIR"
  umask 077
  mkdir "$GENII_USER_DIR"
  # copy the existing state to our specially named folder.
  cp -Rf "$OLD_STATE_DIR"/*.xml "$OLD_STATE_DIR"/*.txt "$OLD_STATE_DIR"/*.dat "$GENII_USER_DIR" &>$TEST_TEMP/state_dir_copy-$user.log
  echo "User now has a state directory in $GENII_USER_DIR"
  establish_identity ${user} ${password} ${idp_path}
  if [ $? -ne 0 ]; then
    echo "Failed to log in using our new state folder."
    return 2
  fi

  echo "I now am logged in as this guy:"
  grid whoami
  cat $GRID_OUTPUT_FILE

  return 0
}

# requires a user and a password, and then causes the grid client to become that user.
# can optionally take an IDP path to perform authentication against.
function establish_identity()
{
  local user="$1"; shift
  local password="$1"; shift
  local idp_path="$1"; shift
  if [ -z "$user" -o -z "$password" ]; then
    echo establish_identity requires a user and password to become an identity.
  fi
#echo "establish_identity: idp_path is: $idp_path"
  # now toss any old creds.
  grid logout --all
  if [ "$BES_TYPE" = "Unicore" ]; then
    # make sure we do the extra step for unicore authentication, if it seems
    # needed.  this step has to be done inside one grid client activation to
    # ensure the tool doesn't re-identify itself in between any steps.
    multi_grid <<eof
      logout --all
      keystoreLogin --toolIdentity --password=$KEYSTORE_PASSWORD --validDuration=10years local:$KEYSTORE_FILE
      login --username=${user} --password=${password} $idp_path
eof
  else
    # assert the new identity.  this is the big one that had better work.
    grid login --username=${user} --password=${password} $idp_path
  fi
}

# removes the separate user identity in terms of the running script.
# this does nothing to the grid files, but it logs out the user and
# gets rid of the local state directory.
take_off_hat()
{
  # check to make sure we don't whack a legitimate grid state directory.
  if [ ! -z "$(echo "$GENII_USER_DIR" | grep genesisII)" ]; then
    echo "Danger: it looks like take_off_hat was called before put_on_hat."
    return 2
  fi
  if [ -z "$(echo "$GENII_USER_DIR" | grep $DIR_KEYWORD)" ]; then
    echo "Danger: it looks like put_on_hat was not called properly."
    return 2
  fi
  echo "About to log out from $GENII_USER_DIR"
  # drop the login now, since that user is done.
  grid logout --all
  echo "Logged out, now cleaning up $GENII_USER_DIR"
  # removing the copy of the directory, we fervently hope.
  rm -rf "$GENII_USER_DIR"
  GENII_USER_DIR="$OLD_STATE_DIR"
  echo "Done taking off hat for multi-user."
}

# a helper function that logs out to remove old credentials, then pops up
# a GUI login dialog to get the user to login.
function login_a_user()
{
  admin="$1"; shift
  grid logout --all
  assertEquals "Logging out of the grid" 0 $?
  echo "You are about to be requested to login."
  if [ "$admin" == "admin" ]; then
    echo "Please ensure that you log in with administrative rights, because this account"
    echo "will need to be able to create users on the grid."
  else
    echo "Please ensure that you log in as a normal (non-admin) user.  This account will"
    echo "be used for all further testing in this test suite."
  fi
  grid login
  local retval=$?
  assertEquals "Logging in to the grid as an administrative user" 0 $retval
  if [ $retval == 0 ]; then
    # a very simplistic check that some id got mapped to some certificate.
    local my_output="$(mktemp $TEST_TEMP/grid_logs/out_login_user.XXXXXX)"
    local grid_app="$(pick_grid_app)"
    betterBeUs=$(raw_grid \"$grid_app\" whoami 2>&1 | grep '" -> "' &>"$my_output")
    retval=${PIPESTATUS[0]}
    rm -f "$my_output"
    assertEquals "Checking for non-vanilla credentials" 0 $retval
  fi
  if [ $retval != 0 ]; then
    fail "Bailing out since the login process did not succeed"
    exit 3
  fi
}

# performs the steps necessary to create a user and her home folder, and to link in
# access to the group of choice.
function create_user() {
  local full_user="$1"; shift
  local passwd="$1"; shift
  local full_group="$1"; shift
  if [ -z "$full_user" -o -z "$passwd" -o -z "$full_group" ]; then
    echo "Error in create_user function--need to specify user, password & group."
    exit 1
  fi
  local user=$(basename $full_user)
  if [ "$user" == "$full_user" ]; then
    full_user="$USERS_LOC/$user"
  fi
  local grp=$(basename $full_group)
  if [ "$grp" == "$full_group" ]; then
    full_group="$GROUPS_LOC/$grp"
  fi

  grid script "local:'$XSEDE_TEST_ROOT/library/create_one_user.xml'" "$STS_LOC" "$user" "$full_user" "$passwd" "$grp" "$full_group" "$HOMES_LOC" "$(dirname "$full_user")"
}

# creates a group in the grid.  this group is not expected to have any special
# permissions.
function create_group()
{
  local full_group="$1"; shift
  if [ -z "$full_group" ]; then
    echo "Error in create_group function--need to specify group name."
    exit 1
  fi
  local grp="$(basename $full_group)"
  grid ls "$STS_LOC/Services/X509AuthnPortType/$grp" &>/dev/null
  if [ $? -ne 0 ]; then
    multi_grid <<eof
      idp --validDuration=10years $STS_LOC/Services/X509AuthnPortType "$grp"
      onerror Failed to create group for $grp using $STS_LOC.
      ln $STS_LOC/Services/X509AuthnPortType/"$grp" "$full_group"
      onerror Failed to add $grp group link for to groups directory.
eof
  fi
}

# makes a resource usable by a group or user.  the resource is the first
# parameter, and the group or user is the second.
function give_create_perms()
{
  local resrc="$1"; shift
  local grp="$1"; shift
  echo "Granting create permissions to '$resrc' for '$grp'."
  grid_chk chmod "$resrc" +rx "$grp"
}

# gets the groups found under a user path.  this is handy for later removing
# those groups with an unlink (the proper way) instead of just recursively
# deleting the user entry.
function listGroups()
{
  local userpath=$1; shift
  "$GENII_INSTALL_DIR/grid" ls $userpath | tail -n +2
}

# safely clean out any groups listed under a user path.
function unlinkGroupsUnderUser()
{
  local userpath=$1; shift
  local grplist="$(listGroups $userpath)"
  local grp
  for grp in $grplist; do 
    if [ ! -z "$grp" ]; then
      grid_chk unlink $userpath/$grp
    fi
  done 
}

