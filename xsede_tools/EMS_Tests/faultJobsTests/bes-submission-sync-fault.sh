#!/bin/bash

##Author: Vanamala Venkataswamy

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

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

		grid run --jsdl=local:$GENERATED_JSDL_FOLDER/application-core-fault.jsdl $i
		assertNotEquals "Submitting  '/bin/cat' job with apllication-core-fault on $i" 0 $?

	done
}

oneTimeTearDown()
{
  grid rm -rf $RNSPATH/test-bes-async &>/dev/null
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

