#!/bin/bash

## Author: Vanamala Venkataswamy
## Date: 01-10-2012

## This script runs all the DATA related grid commands, each test in turn invokes an XScript
## that runs the commands and associated options.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

	if [ -e ./fuse_mnt ]
	then
		fusermount -u ./fuse_mnt 2>/dev/null 1>/dev/null
	else
		mkdir ./fuse_mnt
	fi

}

testCatCommand()
{
  grid script local:./gridCmd_cat.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'cat' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testCdCommand()
{
  grid script local:./gridCmd_cd.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'cd' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testCpCommand()
{
  grid script local:./gridCmd_cp.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'cp' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testExportCommand()
{
  grid script local:./gridCmd_export.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'export' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testFuseCommand()
{
  grid script local:./gridCmd_fuse.xml $RNSPATH ./fuse_mnt
  retval=$?
  assertEquals "Testing 'fuse' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testLnandUnlinkCommand()
{
  grid script local:./gridCmd_ln_unlink.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'ln and unlink' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testLsCommand()
{
  grid script local:./gridCmd_ls.xml $RNSPATH
  retval=$?
  assertEquals "Testing 'ls' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testMkdirCommand()
{
  grid script local:./gridCmd_mkdir.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'mkdir' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testMvCommand()
{
  grid script local:./gridCmd_mv.xml $RNSPATH 
  retval=$?
  assertEquals "Testing 'mv' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}

testRmCommand()
{
  grid script local:./gridCmd_rm.xml $RNSPATH $CONTAINERPATH
  retval=$?
  assertEquals "Testing 'rm' command and its options" 0 $retval
  if [ $retval != 0 ]; then
    fail "Bailing on rest of test because resources are missing."
    exit 1
  fi
}


oneTimeTearDown()
{

	if [ -e ./fuse_mnt ]
        then
                fusermount -u ./fuse_mnt 2>/dev/null 1>/dev/null
		rmdir ./fuse_mnt
	fi
}

# load and run shUnit2
source $SHUNIT_DIR/shunit2

