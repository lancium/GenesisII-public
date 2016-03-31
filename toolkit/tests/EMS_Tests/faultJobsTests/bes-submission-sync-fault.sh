#!/bin/bash

#Author: Vanamala Venkataswamy
#mods: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

  echo "Copying necessary file to Grid namespace"
  grid cp local:./hostname.sh grid:$RNSPATH
  grid cp local:./cat.sh grid:$RNSPATH
  grid cp local:./seg-fault.c grid:$RNSPATH
  grid cp local:./appl-seg-fault.sh grid:$RNSPATH

  grid rm -rf $RNSPATH/test-bes-async &>/dev/null
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testFaultyJobs()
{
    
	for i in $available_resources; do
		grid run --jsdl=local:$GENERATED_JSDL_FOLDER/ls-malformed.jsdl $i	
		assertNotEquals "Submitting single malformed '/bin/ls' job on $i" 0 $?

		grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-stagein-error.jsdl $i
		assertNotEquals "Submitting  '/bin/cat' job with NO stagein on $i" 0 $?

		grid run --jsdl=local:$GENERATED_JSDL_FOLDER/cat-stageout-error.jsdl $i
		assertNotEquals "Submitting  '/bin/cat' job with WRONG stageout on $i" 0 $?

		if [ "$BES_TYPE" != "Unicore" ]; then
			grid run --jsdl=local:$GENERATED_JSDL_FOLDER/application-core-fault.jsdl $i
			assertEquals "Submitting  '/bin/cat' job with application-core-fault on $i" 0 $?
			# should work to run above job, but it also should produce an error file.
                        grid cp $RNSPATH/seg-err.txt local:$TEST_TEMP/seg-err.txt
			assertEquals "Copying expected failure file for application-core-fault on $i" 0 $?
			if [ -s $TEST_TEMP/seg-err.txt ]; then
				true
			else
				false
			fi
			assertEquals "Checking expected failure file for application-core-fault on $i" 0 $?
		fi
	done
}

oneTimeTearDown()
{
  grid rm -rf $RNSPATH/test-bes-async &>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

