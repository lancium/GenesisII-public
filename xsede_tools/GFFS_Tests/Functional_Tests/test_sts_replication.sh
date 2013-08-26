#!/bin/bash
#
# Author: High-Performance-Computing Team, UVa

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

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

function backupEnabled()
{
  if [ -z "$BACKUP_DEPLOYMENT_NAME" -o -z "$BACKUP_USER_DIR" -o -z "$BACKUP_USER_DIR" \
      -o -z "$BACKUP_PORT_NUMBER" ]; then
    return 1  # not enabled.
  else
    return 0  # zero exit is success; is enabled.
  fi
}

oneTimeSetUp()
{
  sanity_test_and_init
}

testX509AuthnPortTypeReplication()
{
  if ! backupEnabled; then return 0; fi
  echo "Testing X509Authentication PortType Replication"

  # logout from the current account and get root privileges
  grid logout --all
  get_root_privileges
  
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

  # give a few moment for updates to propagate into the replica
  sleep 45

  # shutdown the backup container
  bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh "$BACKUP_DEPLOYMENT_NAME"

  # wait a few moments to ensure that backup container shutdown did happen 
  sleep 5

  # link the groups within the user (updates happenning only on the replica located in the primary container)
  grid_chk ln $GROUPS_LOC/replicatedGroup1 $USERS_LOC/replicatedUser/replicatedGroup1
  grid_chk ln $GROUPS_LOC/replicatedGroup2 $USERS_LOC/replicatedUser/replicatedGroup2

  # discard root privileges and login to the replicated user
  grid_chk logout --all
  grid_chk login --username=replicatedUser --password=test
  grid whoami
  cat $GRID_OUTPUT_FILE 
 
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
  grid_chk rm -rf sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup1
  grid_chk rm -rf sampleDir
  grid_chk cd $HOMES_LOC/replicatedGroup2
  grid_chk rm -rf sampleDir
  
  # logout from the user account and get back root privileges
  grid_chk logout --all
  get_root_privileges

  # remove the second group from the user (both containers should be updated)
  grid_chk rm -rf $USERS_LOC/replicatedUser/replicatedGroup2

  # wait a few moment for replica synchronization
  sleep 45

  # again shutdown the backup container
  bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh "$BACKUP_DEPLOYMENT_NAME"

  # login again to the user; this time one group credential should be missing
  # do some parsing of whoami to ensure that certificate count is as expected (Not Done)
  grid_chk logout --all
  grid login --username=replicatedUser --password=test
  grid whoami
  cat $GRID_OUTPUT_FILE

  # test is successfull so far; get back root privileges and start cleanup
  get_root_privileges
  grid_chk cd /

  # ---------------------------- get rid of STS resources from common directories
  grid_chk rm -rf $USERS_LOC/replicatedUser
  grid_chk rm -rf $GROUPS_LOC/replicatedGroup1
  grid_chk rm -rf $GROUPS_LOC/replicatedGroup2
  #----------------------------- get rid of home directories
  grid_chk rm -rf $HOMES_LOC/replicatedUser
  grid_chk rm -rf $HOMES_LOC/replicatedGroup1
  grid_chk rm -rf $HOMES_LOC/replicatedGroup2

  # restart the backup container (takes some time; so wait)
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

  # let some time to pass to allow primary STS resources being destroyed in the backup container
  echo "waiting 5 minutes to allow synchronization of two containers"
  sleep 5m

  # get rid of STS EPRs from the port-type directory
  grid_chk rm -rf $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedUser
  grid_chk rm -rf $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup1
  grid_chk rm -rf $BACKUP_CONTAINER/Services/X509AuthnPortType/replicatedGroup2

  # restore the container path to primary
  export CONTAINERPATH=$oldContainer
}

testKerberosPortTypeReplication() {
  if ! backupEnabled; then return 0; fi
  echo "Testing Kerberos PortType Replication"

  # logout from the current account and get root privileges 
  grid logout --all
  get_root_privileges
  
  # create a Kerberos IDP instance in the backup container and link it in the primary
  grid_chk idp --kerbRealm=$kerberosRealm --kerbKdc=$kerberosKdc $BACKUP_CONTAINER/Services/KerbAuthnPortType $userName
  grid_chk ln $BACKUP_CONTAINER/Services/KerbAuthnPortType/$userName $USERS_LOC/replicatedKerbUser
  
  # replicate the user in the primary container
  grid_chk resolver -p -r $USERS_LOC/replicatedKerbUser $CONTAINERPATH
  grid_chk replicate -p $USERS_LOC/replicatedKerbUser $CONTAINERPATH

  # create a home directory for the kerberos user and give him permission to it
  grid_chk mkdir $HOMES_LOC/replicatedKerbUser
  grid_chk chmod $HOMES_LOC/replicatedKerbUser +r+w+x $USERS_LOC/replicatedKerbUser

  # logout from the root account
  grid logout --all

  # shutdown the backup container 
  bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh "$BACKUP_DEPLOYMENT_NAME"

  # login to the kerberos user
  grid_chk login --username=replicatedKerbUser --password=$kerberosPassword

  # go to the user home directory and do some updates
  grid_chk cd $HOMES_LOC/replicatedKerbUser
  grid_chk mkdir sampleDir
  grid_chk rm -rf sampleDir

  # logout from the kerberos user and get back root privileges
  grid_chk logout --all
  get_root_privileges

  # cleanup the common directories
  grid_chk rm -rf $HOMES_LOC/replicatedKerbUser
  grid_chk rm -rf $USERS_LOC/replicatedKerbUser

  # restart the backup container
  save_and_switch_userdir "$BACKUP_USER_DIR"
  launch_container_if_not_running "$BACKUP_DEPLOYMENT_NAME"
  restore_userdir

  # let some time to pass to allow propagation of user destroy from primary to the backup container
  sleep 1m

  # remove the kerberos user EPR from port-type directory 
  grid_chk rm -rf $BACKUP_CONTAINER/Services/KerbAuthnPortType/$userName

  # get rid of the root privileges
  grid_chk logout --all
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

