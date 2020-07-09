#!/bin/bash

#Author: Vanamala Venkataswamy
#mods: Chris Koeritz, Charlie Houghton

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

getAdminCredentials()
{
	grid login /users/xsede.org/admin --username=admin --password=admin
}

dropAdminCredentials()
{
	grid logout --pattern=admin
	grid logout --pattern=gffs-users
	grid logout --pattern=gffs-amie
	grid login /groups/xsede.org/gffs-users --username=gffs-users --password=""
}

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  create_jsdl_from_templates  # turn template files into the real jsdl.
  if [ $? -ne 0 ]; then
    echo "JSDL file generation failure.  unassigned random id perhaps?"
    exit 1
  fi

	echo "Adding wrappers to bes-activities directory"
	cp singularity-wrapper.sh ~/.genesisII-2.0/bes-activities/
	cp vmwrapper.sh ~/.genesisII-2.0/bes-activities/

  echo "Copying necessary file to Grid namespace:" $RNSPATH
  grid cp local:$PWD/inside-container.sh grid:$RNSPATH

	echo "Logging into admin to create directories"
	getAdminCredentials
	echo "Adding image to Images userX's Image dir"
	grid mkdir -p /home/CCC/Lancium/userX/Images
	grid chmod -R /home/CCC +rwx /users/xsede.org/userX
	dropAdminCredentials
  grid cp local:$PWD/ubuntu18.04.simg grid:/home/CCC/Lancium/userX/Images/ubuntu18.04.simg
  grid cp local:$PWD/ubuntu18.04.qcow2 grid:/home/CCC/Lancium/userX/Images/ubuntu18.04.qcow2

	echo "Adding local FS Images dir"
	mkdir -p ~/.genesisII-2.0/bes-activities/Images/userX/
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testSingularityImageJob()
{
	submit="$(grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/singularity-image-job.jsdl)"
	assertEquals "Submitting singularity image job" 0 $?
	echo "Waiting 15 seconds for job to finish and data to be staged out."
	sleep 15
	grid ls grid:$RNSPATH
	grid cat grid:$RNSPATH/singularity-image-job.out
	hostname_sleep_output="$(grid cat grid:$RNSPATH/singularity-image-job.out)"
	echo $hostname_sleep_output | grep -q "singularity-wrapper, image: ../Images/userX/ubuntu18.04.simg, args: /bin/bash inside-container.sh"
	assertEquals "Image job should be using the wrapper and have the arguments /bin/bash inside-container.sh." 0 $?
}

testVMImageJob()
{
	submit="$(grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/vm-image-job.jsdl)"
	assertEquals "Submitting vm image job" 0 $?
	echo "Waiting 15 seconds for job to finish and data to be staged out."
	sleep 15
	grid ls grid:$RNSPATH
	grid cat grid:$RNSPATH/vm-image-job.out
	hostname_sleep_output="$(grid cat grid:$RNSPATH/vm-image-job.out)"
	echo $hostname_sleep_output | grep -q "vmwrapper, image: ../Images/userX/ubuntu18.04.qcow2, args: /bin/bash inside-container.sh"
	assertEquals "Image job should be using the wrapper and have the arguments /bin/bash inside-container.sh." 0 $?
}

oneTimeTearDown()
{
  echo tearing down test.
	getAdminCredentials
	grid rm -r /home/CCC
	dropAdminCredentials
	grid rm grid:$RNSPATH/vm-image-job.out
	grid rm grid:$RNSPATH/vm-image-job.err
	grid rm grid:$RNSPATH/singularity-image-job.out
	grid rm grid:$RNSPATH/singularity-image-job.err
	grid rm grid:$RNSPATH/inside-container.sh
	rm -r ~/.genesisII-2.0/bes-activities/Images
	rm ~/.genesisII-2.0/bes-activities/singularity-wrapper.sh
	rm ~/.genesisII-2.0/bes-activities/vmwrapper.sh
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

