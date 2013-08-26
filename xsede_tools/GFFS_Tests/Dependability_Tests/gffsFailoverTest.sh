#!/bin/bash

##Author: Sal Valente

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

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

testFailover()
{
  if ! backupEnabled; then return 0; fi

  grid cd $RNSPATH
  if grid ping dir3 &>/dev/null; then
    grid rm -rf dir3
  fi

  # Create a directory with the first instance in the backup container
  # and auto-replication to the bootstrap container.
  grid mkdir --rns-service=${BACKUP_CONTAINER}/Services/EnhancedRNSPortType dir3
  grid resolver -p dir3 ${LOCAL_CONTAINER}
  assertEquals "add resolver to directory" 0 $?
  grid replicate -p dir3 ${LOCAL_CONTAINER}
  assertEquals "replicate directory" 0 $?
  sleep 1

  # Create a local tree
  localtree=/tmp/failover$$
  mkdir "$localtree"
  mkdir "${localtree}/A"
  mkdir "${localtree}/B"
  cp /bin/cp "${localtree}/A/C"
  cp /bin/rm "${localtree}/A/D"
  mkdir "${localtree}/B/E"
  cp /bin/ls "${localtree}/B/F"
  # Create a 70 MB file
  p=/usr/bin/perl
  cat $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p $p > "${localtree}/B/E/G"

  # Copy local tree into replicated GFFS space
  grid cp -r "file:${localtree}" "dir3/tree"
  assertEquals "copy local tree to remote" 0 $?
  echo 'Directories and files have been created.'
  echo 'Replication should be taking place in a background thread.'
  echo 'Kill the backup container and hit Enter.'
  read response
  echo 'Thank you.'

  # Copy remote tree local.
  localcopy=/tmp/duplicate$$
  grid cp -r "dir3/tree" "file:${localcopy}"
  diff -c -r "$localtree" "$localcopy"
  assertEquals "compare replicated data to original data" 0 $?

  rm -rf "$localtree"
  rm -rf "$localcopy"
  echo 'Failover testing is complete.'
  echo 'You may restart the backup container.'
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2
