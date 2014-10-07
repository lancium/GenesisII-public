#!/bin/bash
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

export MIRRORPATH="$BACKUP_CONTAINER"

#userName=$1;shift
#kerberosPassword=$1;shift

#if [ -z "$userName" ]; then
#  userName="xsedeKrbTester"
#fi
#if [ -z "$kerberosPassword" ]; then
#  kerberosPassword="LimonadoDavinci83"
#fi
#
#kerberosRealm="UVACSE"
#kerberosKdc="uvacse-002.cs.virginia.edu"

function oneTimeSetUp()
{

  # get rid of previous privileges.
  grid_chk logout --all

  get_root_privileges
  assertEquals "acquiring root privileges on grid" 0 $?

  sanity_test_and_init
  if ! isMirrorEnabled; then 
    # nothing to do.
    return 0
  fi

  # back up container state for restoring later.
  ROOT_SAVE_DIR="$(mktemp -d $TEST_TEMP/rootcontainersave.XXXXXX)"
  \cp -R $GENII_USER_DIR/* "$ROOT_SAVE_DIR"
  BACKUP_SAVE_DIR="$(mktemp -d $TEST_TEMP/backupcontainersave.XXXXXX)"
  \cp -R $BACKUP_USER_DIR/* "$BACKUP_SAVE_DIR"

}

function testCreatingResolvers()
{
  if ! isMirrorEnabled; then 
    echo "No backup deployment found; exiting test."
    return 0
  fi

  bash $XSEDE_TEST_ROOT/tools/xsede_admin/top_level_replication_setup.sh localhost $BACKUP_PORT_NUMBER
  check_if_failed "Running top-level replication script"
}

function createUserAndReplicate()
{
  local user="$1"; shift
  local password="$1"; shift

  if [ -z "$user" -o -z "$password" ]; then
    echo "this script needs a user (short version) and a password to create."
    exit 1
  fi

  echo creating user $user
  bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh \
    "/users/xsede.org/$user" "$password" \
    /groups/xsede.org/gffs-users /home/xsede.org
  check_if_failed "creating user $user"
  
  echo adding resolver for $user
  $GENII_INSTALL_DIR/grid resolver "/users/xsede.org/$user" /etc/resolvers/rootResolver
#-p -r 
  check_if_failed "adding resolver for $user"

  echo replicating $user
  $GENII_INSTALL_DIR/grid replicate "/users/xsede.org/$user" $MIRRORPATH
#-p 
  check_if_failed "replicating $user"
}

function testAddNewUser()
{
  # get logged back in after replication dropped us out.
  get_root_privileges
  assertEquals "acquiring root privileges on grid" 0 $?

  createUserAndReplicate grady pony
  check_if_failed "creating user and replicating it"

#already done by above create step.
#  $GENII_INSTALL_DIR/grid script local:$XSEDE_TEST_ROOT/tools/xsede_admin/link-user-to-group.xml /users/xsede.org grady /groups/xsede.org gffs-users
  #check_if_failed "linking user to gffs-users group"

}

function testReplicationSoFar()
{
  # snooze to allow replication to occur.
  sleep 45

  echo "stopping root container"
  bash "$XSEDE_TEST_ROOT/library/zap_genesis_javas.sh" "$DEPLOYMENT_NAME"

  grid logout --all
  grid login --username=grady --password=pony

  echo user credentials with only replica running:
  grid whoami
  cat $GRID_OUTPUT_FILE
  grep -q gffs-users $GRID_OUTPUT_FILE
  check_if_failed "could not find gffs-users in the new user's credentials"
  

echo test here would do more, but you can check things out after the replication.
echo backup container is stopped.
return 0
  
}

oneTimeTearDown()
{
echo not tearing down yet.
return 0;


  if ! isMirrorEnabled; then 
    # nothing to do.
    return 0
  fi

  # stop the containers.
  bash "$XSEDE_TEST_ROOT/library/zap_genesis_javas.sh" "$DEPLOYMENT_NAME"
  bash "$XSEDE_TEST_ROOT/library/zap_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"

  # put the saved container state back in.
  \cp "$ROOT_SAVE_DIR"/* $GENII_USER_DIR
  \rm -rf "$ROOT_SAVE_DIR"
  \cp "$BACKUP_SAVE_DIR"/* $BACKUP_USER_DIR
  \rm -rf "$ROOT_SAVE_DIR"

  # restart the containers.
  launch_container_if_not_running "$DEPLOYMENT_NAME"
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

