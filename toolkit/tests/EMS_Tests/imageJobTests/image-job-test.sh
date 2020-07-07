#!/bin/bash

#Author: Vanamala Venkataswamy
#mods: Chris Koeritz, Charlie Houghton

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

	echo "Adding singularity-wrapper.sh"
	cp singularity-wrapper.sh ~/.genesisII-2.0/bes-activities/

  echo "Copying necessary file to Grid namespace:" $RNSPATH
  grid cp local:$PWD/inside-container.sh grid:$RNSPATH

	echo "Logging into admin to create directories"
	grid login /users/xsede.org/admin --username=admin --password=admin
	echo "Adding image to Images userX's Image dir"
	grid mkdir -p /home/CCC/Lancium/userX/Images
	grid chmod -R /home/CCC/ +rw /users/xsede.org/userX
	grid logout --pattern=admin
	grid logout --pattern=gffs-users
	grid logout --pattern=gffs-aime
	grid login /groups/xsede.org/gffs-users --username=gffs-users --password=""
  grid cp local:$PWD/ubuntu18.04.simg grid:/home/CCC/Lancium/userX/Images/ubuntu18.04.simg

	echo "Adding local FS Images dir"
	mkdir -p ~/.genesisII-2.0/bes-activities/Images/userX/
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testImageJob()
{
	submit="$(grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/image-job.jsdl)"
	assertEquals "Submitting single job that we intend to terminate in 30 seconds with file staging from GFFS using OGSA BYTE/IO protocol" 0 $?
	echo "Waiting 60 seconds for job to finish and data to be staged out."
	sleep 60
	grid ls grid:$RNSPATH
	grid cat grid:$RNSPATH/image-job.out
	hostname_sleep_output="$(grid cat grid:$RNSPATH/image-job.out)"
	echo $hostname_sleep_output | grep -q "args: /bin/bash inside-container.sh"
	assertEquals "Image job should be using the wrapper and have the arguments /bin/bash inside-container.sh." 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
	grid rm -r /home/CCC
	rm -r ~/.genesisII-2.0/bes-activities/Images
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

