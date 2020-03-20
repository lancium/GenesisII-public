#!/bin/bash
#
# Author: High-Performance-Computing Team, UVa

#hmmm: this test doesn't seem to clean up after itself.  should be able to run repeatedly.
#      ah, this may be due to the test being interrupted in process; in the normal case of it
#      running to completion, it does clean up by restoring the backed up bootstrap grid state.
#      part of the problem is that grid_chk is used a lot, which exits the script when an 
#      error is found.  instead, it should use grid and check the error result and then skip
#      remaining tests (but not cleanup) when an error is found.  otherwise, this test causes
#      everything after it in the build to behave very badly.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

userName=$1;shift
kerberosPassword=$1;shift

if [ -z "$userName" ]; then
  userName="xsedeKrbTester"
fi
if [ -z "$kerberosPassword" ]; then
  kerberosPassword="LimonadoDavinci83"
fi

kerberosRealm="UVACSE"
kerberosKdc="uvacse-002.cs.virginia.edu"

function oneTimeSetUp()
{
  sanity_test_and_init

  USER_SAVE_DIR="$(mktemp -d $TEST_TEMP/user-save.XXXXXX)"
  echo "saving user state in $USER_SAVE_DIR"
  # back up user's login state for reverting later.
  \cp $GENII_USER_DIR/user* "$USER_SAVE_DIR"

  # get rid of previous privileges.
  grid_chk logout --all
}

function testX509AuthnPortTypeReplication()
{
  if ! isMirrorEnabled; then 
    echo "No backup deployment found; exiting test."
    return 0
  fi
  echo "Testing X509Authentication PortType Replication"

  # become a super user for the grid.
  get_root_privileges
  assertEquals "acquiring root privileges on grid" 0 $?
  
  # backup and update the container path
  oldContainer=$CONTAINERPATH
  export CONTAINERPATH=$BACKUP_CONTAINER
  
  # create two groups in the backup container and link them in primary
  grid_chk idp $BACKUP_CONTAINER/Services/X509AuthnPortType replicatedGroup1
  grid_chk idp $BACKUP_CONTAINER/Services/X509AuthnPortType replicatedGroup2
  grid_chk ln $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup1 $GROUPS_LOC/replicatedGroup1
  grid_chk ln $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup2 $GROUPS_LOC/replicatedGroup2
  
  # create a user in the backup container and link it in primary
  grid_chk create-user $BACKUP_CONTAINER/Services/X509AuthnPortType replicatedUser --login-name=replicatedUser --login-password=test
  grid_chk ln $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedUser $USERS_LOC/replicatedUser

  # replicate all STS resources in the primary
  # ----------------- first step: attach resolvers to resources
  grid_chk resolver -p -r $USERS_LOC/replicatedUser $oldContainer
  grid_chk resolver -p -r $GROUPS_LOC/replicatedGroup1 $oldContainer
  grid_chk resolver -p -r $GROUPS_LOC/replicatedGroup2 $oldContainer
  # ----------------- second step: do the replication
  grid_chk replicate -p $USERS_LOC/replicatedUser $oldContainer
  grid_chk replicate -p $GROUPS_LOC/replicatedGroup1 $oldContainer
  grid_chk replicate -p $GROUPS_LOC/replicatedGroup2 $oldContainer

  # create directories for GFFS access checking
  grid_chk mkdir $HOMES_LOC/replicatedUser
  grid_chk mkdir $HOMES_LOC/replicatedGroup1
  grid_chk mkdir $HOMES_LOC/replicatedGroup2
 
  # assign permissions to the created directories
  grid_chk chmod $HOMES_LOC/replicatedUser +r+w+x $USERS_LOC/replicatedUser
  grid_chk chmod $HOMES_LOC/replicatedGroup1 +r+w+x $GROUPS_LOC/replicatedGroup1
  grid_chk chmod $HOMES_LOC/replicatedGroup2 +r+w+x $GROUPS_LOC/replicatedGroup2

  # give the user permission to the groups (updates of groups happening through the backup container)
  grid_chk chmod $GROUPS_LOC/replicatedGroup1 +r+x $USERS_LOC/replicatedUser
  grid_chk chmod $GROUPS_LOC/replicatedGroup2 +r+x $USERS_LOC/replicatedUser

  grid_chk chmod $GROUPS_LOC/gffs-users +r+x $USERS_LOC/replicatedUser
  grid_chk ln $GROUPS_LOC/gffs-users $USERS_LOC/replicatedUser/gffs-users

  # give a few moment for updates to propagate into the replica
  sleep 45

  # shutdown the backup container
  bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
  if [ $? -ne 0 ]; then
    echo "shutting down the backup container failed!"
    exit 1
  fi

echo ====
echo listing all containers running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" 
echo listing just backup to see if running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
echo ====

  # wait a few moments to ensure that backup container shutdown did happen 
  sleep 5

  # link the groups within the user (updates happenning only on the replica located in the primary container)
  grid_chk ln $GROUPS_LOC/replicatedGroup1 $USERS_LOC/replicatedUser/replicatedGroup1
  grid_chk ln $GROUPS_LOC/replicatedGroup2 $USERS_LOC/replicatedUser/replicatedGroup2

  # discard root privileges and login to the replicated user
  grid_chk logout --all
  grid_chk login --username=replicatedUser --password=test
  grid whoami
#  cat $GRID_OUTPUT_FILE 
 
  # write to each of the three home directories to ensure that the credentials are appropriate  
  grid_chk cd $HOMES_LOC/replicatedUser
  grid_chk mkdir sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup1
  grid_chk mkdir sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup2
  grid_chk mkdir sampleDir

  # logout from the user account and get back root privileges
  grid_chk logout --all
  get_root_privileges
  grid_chk cd /

  # restart the backup container (takes some time; so wait)
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

  # sleep a little bit to allow replicas to be sychronized with the primary
  echo "waiting 5 minutes to allow synchronization of two containers"
  sleep 5m

  # logout from root and login to the user account (this time primary STS resources will be invoked)
  grid_chk logout --all
  grid login --username=replicatedUser --password=test
  grid whoami
  cat $GRID_OUTPUT_FILE 

  # remove the previously created directories inside /home
  grid_chk cd $HOMES_LOC/replicatedUser
echo ====
grid pwd
grid ls 
echo ====
  grid_chk rm -r sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup1
echo ====
grid pwd
grid ls 
echo ====
  grid_chk rm -r sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup2
echo ====
grid pwd
grid ls 
echo ====
  grid_chk rm -r sampleDir
  
  # logout from the user account and get back root privileges
  grid_chk logout --all
  get_root_privileges

  # remove the second group from the user (both containers should be updated)
  grid_chk unlink $USERS_LOC/replicatedUser/replicatedGroup2

  # wait a few moment for replica synchronization
  sleep 45

  # again shutdown the backup container
  bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
  if [ $? -ne 0 ]; then
    echo "shutting down the backup container failed!"
    exit 1
  fi

echo ====
echo listing all containers running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" 
echo listing just backup to see if running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
echo ====

  # login again to the user; this time one group credential should be missing
  # do some parsing of whoami to ensure that certificate count is as expected (Not Done)
  grid_chk logout --all
  grid login --username=replicatedUser --password=test
  grid whoami
  cat $GRID_OUTPUT_FILE
}

function testCleaningUpX509()
{
  # test is successfull so far; get back root privileges and start cleanup
  get_root_privileges
  grid_chk cd /

  # ---------------------------- get rid of STS resources from common directories
  unlinkGroupsUnderUser $USERS_LOC/replicatedUser
  grid_chk unlink $USERS_LOC/replicatedUser
  grid_chk unlink $GROUPS_LOC/replicatedGroup1
  grid_chk unlink $GROUPS_LOC/replicatedGroup2
  #----------------------------- get rid of home directories
  grid_chk rm -r $HOMES_LOC/replicatedUser
  grid_chk rm -r $HOMES_LOC/replicatedGroup1
  grid_chk rm -r $HOMES_LOC/replicatedGroup2

  # restart the backup container (takes some time; so wait)
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

  # let some time to pass to allow primary STS resources being destroyed in the backup container
  echo "waiting 5 minutes to allow synchronization of two containers"
  sleep 5m

  # get rid of STS EPRs from the port-type directory
  grid_chk rm -r $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedUser
  grid_chk rm -r $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup1
  grid_chk rm -r $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup2

  # restore the container path to primary
  export CONTAINERPATH=$oldContainer
}

function testKerberosPortTypeReplication()
{
  echo "Kerberos Replication no longer being tested 2020-03-20 by ASG"
  return
  if ! isMirrorEnabled; then return 0; fi
  echo "Testing Kerberos PortType Replication"

  # logout from the current account and get root privileges 
  grid logout --all
  get_root_privileges
  
  # create a Kerberos IDP instance in the backup container and link it in the primary
  grid_chk idp --kerbRealm=$kerberosRealm --kerbKdc=$kerberosKdc $BACKUP_CONTAINER/Services/KerbAuthnPortType $userName
  grid_chk ln $BACKUP_CONTAINER/Services/KerbAuthnPortType/$userName $USERS_LOC/$userName
  
  # replicate the user in the primary container
  grid_chk resolver -p -r $USERS_LOC/$userName $CONTAINERPATH
  grid_chk replicate -p $USERS_LOC/$userName $CONTAINERPATH

  # create a home directory for the kerberos user and give him permission to it
  grid_chk mkdir $HOMES_LOC/$userName
  grid_chk chmod $HOMES_LOC/$userName +rwx $USERS_LOC/$userName

  grid_chk chmod $GROUPS_LOC/gffs-users +r+x $USERS_LOC/$userName
  grid_chk ln $GROUPS_LOC/gffs-users $USERS_LOC/$userName/gffs-users

  # logout from the root account
  grid logout --all

  # shutdown the backup container 
  bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
  if [ $? -ne 0 ]; then
    echo "shutting down the backup container failed!"
    exit 1
  fi

echo ====
echo listing all containers running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" 
echo listing just backup to see if running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" "$BACKUP_DEPLOYMENT_NAME"
echo listing just main container to see if running...
bash "$GFFS_TOOLKIT_ROOT/library/list_genesis_javas.sh" "$DEPLOYMENT_NAME"
echo ====

  # login to the kerberos user
  grid_chk login --username=$userName --password=$kerberosPassword

  # go to the user home directory and do some updates
  grid_chk cd $HOMES_LOC/$userName
  grid_chk mkdir sampleDir
  grid_chk rm -r sampleDir

  # if all the above worked, then the user was successfully replicated; it was
  # created on the backup container, but then that was shut down.  yet, we
  # were still able to log in as that user with the backup shut down, hence
  # replication to the primary was successful.
}

function testCleaningUpKerb()
{
  echo "Kerberos Replication no longer being tested 2020-03-20 by ASG"
  return
  # logout from the kerberos user and get back root privileges
  grid_chk logout --all
  get_root_privileges
  # pop out of anything we might whack.
  grid_chk cd /

  # cleanup the common directories
  grid_chk rm -r $HOMES_LOC/$userName
  unlinkGroupsUnderUser $USERS_LOC/$userName
  grid_chk unlink $USERS_LOC/$userName

  # restart the backup container
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

  # let some time to pass to allow propagation of user destroy from primary to the backup container
  sleep 2m

  # remove the kerberos user EPR from port-type directory 
  grid_chk rm -r $BACKUP_CONTAINER/Services/KerbAuthnPortType/$userName
}

oneTimeTearDown()
{
  \cp "$USER_SAVE_DIR"/* $GENII_USER_DIR
  \rm -rf "$USER_SAVE_DIR"

  echo these are the user credentials after the test is finished...
  grid whoami
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

