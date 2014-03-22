#!/bin/bash

# this is a replicated STS test that relies on a previously set up virtual
# machine.  the vm lives in the cloud and can be controlled remotely using a
# key file that the tester must receive before testing.  this test also
# validates that a replicated file and folder are available once the serving
# container is shut down.
#
# Author: Muhammad Yanhaona
# Author: Chris Koeritz

# kerberos server information...

# uva version for testing only.
#kerberosRealm="ESERVICES.VIRGINIA.EDU" 
#kerberosKdc="esdc.eservices.virginia.edu"
# xsede version; do not use this except for internal testing.
#kerberosRealm="TERAGRID.ORG"
#kerberosKdc="kerberos.teragrid.org"

# real server for test, plus pre-made account.
kerberosRealm="UVACSE"
kerberosKdc="uvacse-002.cs.virginia.edu"
kerberosUserName="xsedeKrbTester"
kerberosPassword="LimonadoDavinci83"

# amazon host for our VM.
CLOUD_VM="ec2-54-224-154-76.compute-1.amazonaws.com"
# in-grid path for the replication vm's container.
REPLICATION_VM="/containers/replication-vm"

# command line arguments necessary for successful test run.
keyFileForVM=$1;shift
genesisIIAccountName=$1;shift
genesisIIPassword=$1;shift
rnsPath="$1"; shift

if [ -z "$keyFileForVM" -o -z "$genesisIIAccountName" -o -z "$genesisIIPassword" \
      -o -z "$rnsPath" ]; then
  echo "$(basename $0): this script requires four command line parameters:"
  echo "1) key file for the VM under test; the activity personnel should"
  echo "   provide this for testers."
  echo "2) user account name for grid under test.  this can be your xsedeLogin"
  echo "   identity or another valid grid identity, but the user must be a member"
  echo "   of the uva-idp-group or the test will not succeed."
  echo "3) password for above user account."
  echo "4) an RNS path for which the user in parm #2 has write access.  this will"
  echo "   be used to store the created user and group during the test."
  exit 1
fi

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init
}

testStartup() 
{
  echo "testing if the VM container is running"
  grid ping $REPLICATION_VM
  if [ $? -ne 0 ]; then
    echo "VM container is not running; restarting the container"
    ssh -i $keyFileForVM root@"$CLOUD_VM" /home/act123/GenesisII/GFFSContainer start
    echo "Waiting one minute to let the container restart"
    sleep 1m
    echo "checking again if container restart is successfull"
    grid_chk ping $REPLICATION_VM
  fi
  echo "VM container is running; replication test can begin"
  # we will add asserts at end of test if we completely check the results as they occur;
  # this test is structure to bail out for first failure, as rest of test will probably
  # not succeed if our assumption of a working container is invalid.
  assertEquals "startup process" 0 0
}

testInitialLogin()
{
  echo "logout and login again to the user account to get rid of any unexpected credentials"
  grid logout --all
  grid login --username=$genesisIIAccountName --password=$genesisIIPassword
  assertEquals "initial login" 0 $?
}

testEstablishTestingVariables()
{
  usersDir="$rnsPath/repl-test/users"
  groupsDir="$rnsPath/repl-test/groups"
  rnsNewDir="$rnsPath/repl-test/rns"
}

testCleanupVMContainer()
{
  echo "checking if unwanted resources from some previous test run still exist in the grid"
  grid rm -rf $usersDir/kerberosUser &>/dev/null
  grid rm -rf $REPLICATION_VM/Services/KerbAuthnPortType/$kerberosUserName &>/dev/null
  grid rm -rf $REPLICATION_VM/Services/X509AuthnPortType/X509Group &>/dev/null
  grid rm -rf $groupsDir/X509Group &>/dev/null
  grid rm -rf $rnsNewDir &>/dev/null
  assertEquals "cleanup process" 0 0
}

testRNSPath()
{
  # try to make it regardless of whether it exists, and ignore any failure.
  grid mkdir -p "$rnsPath" &>/dev/null
  # now make sure it really does exist.
  grid_chk chmod "$rnsPath" +rwx --everyone
  assertEquals "checking rns path" 0 0

  # default directories to run the test.
  grid mkdir -p "$usersDir" &>/dev/null
  grid_chk chmod "$rnsPath/repl-test" +rwx --everyone
  grid_chk chmod "$usersDir" +rwx --everyone
  grid mkdir -p "$groupsDir" &>/dev/null
  grid_chk chmod "$groupsDir" +rwx --everyone
  assertEquals "creating user and groups directories" 0 0

  # add in an RNS folder on the container.
  grid_chk mkdir --rns-service=$REPLICATION_VM/Services/EnhancedRNSPortType "$rnsNewDir"
  grid_chk chmod "$rnsNewDir" +rwx --everyone
  # copy in this script as an example file.
  grid_chk cp local:"$0" $rnsNewDir/testcopy.sh
  grid_chk chmod "$rnsNewDir/testcopy.sh" +rwx --everyone
  assertEquals "creating rns folder on replication vm" 0 0
}

testCreateUserAndGroup()
{
  echo "creating a Kerberos user and an X.509 group in the VM container"
  grid_chk idp --kerbRealm=$kerberosRealm --kerbKdc=$kerberosKdc $REPLICATION_VM/Services/KerbAuthnPortType $kerberosUserName
  grid_chk ln $REPLICATION_VM/Services/KerbAuthnPortType/$kerberosUserName $usersDir/kerberosUser
  grid_chk idp $REPLICATION_VM/Services/X509AuthnPortType X509Group
  grid_chk ln $REPLICATION_VM/Services/X509AuthnPortType/X509Group $groupsDir/X509Group
  assertEquals "user creation" 0 0
  
  echo "giving the user permission to the group"
  grid_chk chmod $groupsDir/X509Group +r+x $usersDir/kerberosUser
  grid_chk ln $groupsDir/X509Group $usersDir/kerberosUser/X509Group
  assertEquals "group creation" 0 0

  echo "logout from current account and login to the created account"
  echo "this should work as the VM container is running"
  grid logout --all
  grid login --username=kerberosUser --password=$kerberosPassword rns:$usersDir/kerberosUser
  assertEquals "first kerberos login using vm" 0 $?
  grid whoami 
  cat $GRID_OUTPUT_FILE

  echo "logout from the created user account"
  grid logout --all
  grid whoami 
  cat $GRID_OUTPUT_FILE
}

testStopVMContainerforFailureTest()
{
  echo "shutdown the VM container"
  ssh -i $keyFileForVM root@"$CLOUD_VM" /home/act123/GenesisII/GFFSContainer stop
  sleep 10

  echo "try to login again to the created user account; this time login should fail"
  grid logout --all
  grid login --username=kerberosUser --password=$kerberosPassword rns:$usersDir/kerberosUser
  retval=$?
  assertNotEquals "kerberos login with vm down must not succeed" 0 $retval
  if [ $retval -ne 0 ]; then
    echo "login failed as expected"
  fi
}

testLoginGridAccountAgain()
{
  echo "login to the grid user account" 
  grid logout --all
  grid login --username=$genesisIIAccountName --password=$genesisIIPassword
  assertEquals "next login as grid user" 0 $?
  grid whoami 
  cat $GRID_OUTPUT_FILE
}

testRestartVMContainerforReplication()
{
  echo "restart the VM container" 
  ssh -i $keyFileForVM root@"$CLOUD_VM" /home/act123/GenesisII/GFFSContainer start
  echo "Waiting one minute to let the container restart"
  sleep 1m
  echo "checking again if container restart is successfull"
  grid_chk ping $REPLICATION_VM
  assertEquals "vm container restarted" 0 0
}

testReplicateUserAndGroup()
{
  echo "replicating the kerberos user and X509 group in the main grid container"
  grid_chk resolver -p -r $usersDir/kerberosUser $BOOTSTRAP_LOC
  grid_chk replicate -p $usersDir/kerberosUser $BOOTSTRAP_LOC
  grid_chk resolver -p -r $rnsNewDir $BOOTSTRAP_LOC
  grid_chk replicate -p $rnsNewDir $BOOTSTRAP_LOC

  sleep 10
  echo "replication is successful"
  assertEquals "replication of user and group" 0 0
}

testStopVMContainerforReplicationTest()
{
  echo "again shutdown the VM container" 
  ssh -i $keyFileForVM root@"$CLOUD_VM" /home/act123/GenesisII/GFFSContainer stop
  sleep 10
  assertEquals "stopping vm again to test replication" 0 0
}

testReplicatedLogin()
{
  echo "logout from the grid user account and re-login to the created user account; this should succeed"
  grid logout --all
  grid login --username=kerberosUser --password=$kerberosPassword rns:$usersDir/kerberosUser
  retval=$?
  assertEquals "logging in using replicated user entry" 0 $retval
  grid whoami 
  cat $GRID_OUTPUT_FILE

  if [ $retval -eq 0 ]; then
    echo "STS replication test is successful!"
  else
    echo "STS replication test failed!!!!"
  fi

  # now test that the RNS directory and our file are available.
  grid_chk ls $rnsNewDir
  grid_chk cat $rnsNewDir/testcopy.sh
  assertEquals "testing replicated folder and contained file" 0 0

  echo "RNS and ByteIO replication tests were successful!"

  echo "logout from the kerberos account and get back to the grid user account"
  grid logout --all
  grid login --username=$genesisIIAccountName --password=$genesisIIPassword
  assertEquals "logging in to grid identity again near end" 0 $?
}

testRestartVMContainerforCleanup()
{
  echo "restart the VM container for future use"
  ssh -i $keyFileForVM root@"$CLOUD_VM" /home/act123/GenesisII/GFFSContainer start
  echo "Waiting one minute to let the container restart"
  sleep 1m
  echo "checking again if container restart is successfull"
  grid_chk ping $REPLICATION_VM

  echo "do cleanup"
  grid_chk rm -rf $usersDir/kerberosUser
  grid_chk rm -rf $REPLICATION_VM/Services/KerbAuthnPortType/$kerberosUserName
  grid_chk rm -rf $REPLICATION_VM/Services/X509AuthnPortType/X509Group
  grid_chk rm -rf $groupsDir/X509Group

  assertEquals "cleaning test vm" 0 0
}

source $SHUNIT_DIR/shunit2

