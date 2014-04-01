#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Sal Valente

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

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

testReplication()
{
  if ! backupEnabled; then return 0; fi

  grid cd $RNSPATH
  if grid ping dir1 &>/dev/null; then
    grid rm -rf dir1
  fi
  grid mkdir dir1
  assertEquals "grid mkdir" 0 $?
  grid resolver -p dir1 $BACKUP_CONTAINER
  assertEquals "add resolver to directory" 0 $?
  grid replicate -p dir1 $BACKUP_CONTAINER
  assertEquals "replicate directory" 0 $?
  sleep 1

  grid unlink dir2 &>/dev/null
  grid resolver -q dir1 --link=dir2 &> /dev/null
  assertEquals "query resolver" 0 $?

  grid echo "This is file 1" "'>'" dir1/file1.txt
  assertEquals "create replicated file" 0 $?
  grid echo "File 2 is this" "'>'" dir1/file2.txt
  grid echo "Is this file 3" "'>'" dir1/file3.txt
  grid rm dir1/file2.txt
  assertEquals "remove replicated file" 0 $?
  grid mkdir dir1/subdirectory
  grid byteio dir1/file1.txt -a "This_was_appended."
  assertEquals "modify replicated file" 0 $?
  grid byteio dir2/file3.txt -a "Appended_to_replica."
  assertEquals "modify replicated file" 0 $?

  grid rm -f ufile.txt &>/dev/null
  grid echo "This is not replicated" "'>'" ufile.txt
  grid ln ufile.txt dir1/ufile-link.txt
  assertEquals "link unreplicated file in replicated dir" 0 $?


  # dir1 must contain 4 entries with the same address as each other:
  # file1.txt, file3.txt, ufile.txt, and subdirectory
  grid ls -m dir1
  count=(`grep '^address: ' $GRID_OUTPUT_FILE | cut -d/ -f3 | sort | uniq -c`)
  assertEquals "replicated directory contents" 4 "${count[0]}"

  # dir2 must contain 3 entries with the same address as each other:
  # file1.txt, file3.txt and subdirectory
  # and it must contain ufile.txt with a different address.
  sync
  grid ls -m dir2
  count=( $(grep '^address: ' $GRID_OUTPUT_FILE | cut -d/ -f3 | sort |
          uniq -c | sort -nr) )
  assertEquals "replica directory content count" 3 "${count[0]}"
  assertEquals "replica directory link count" 1 "${count[2]}"

  # file1 has the correct two lines and file3 has the correct two lines.
  grid cat dir1/file1.txt dir1/file3.txt
  lines=(`wc -l $GRID_OUTPUT_FILE`)
  assertEquals "replicated file contents" 4 "${lines[0]}"

  cp $GRID_OUTPUT_FILE $TEST_TEMP/out$$
  grid cat dir2/file1.txt dir2/file3.txt
  cmp -s $TEST_TEMP/out$$ $GRID_OUTPUT_FILE
  assertEquals "replica file contents" 0 $?
}

oneTimeTearDown()
{
  grid rm -rf dir1 &>/dev/null
  grid unlink dir2 &>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

