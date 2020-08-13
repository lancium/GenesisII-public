#!/bin/bash

#Author: Vanamala Venkataswamy
#mods: Chris Koeritz, Charlie Houghton

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

hidden_genii_dir=$GENII_USER_DIR
if [ -z "$hidden_genii_dir" ]; then
	hidden_genii_dir=$HOME/.genesisII-2.0
fi

singularity_installed=0

if ! command -v singularity &> /dev/null
then
    singularity_installed=0
else
    singularity_installed=1
fi

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
	cp vmwrapper.sh $hidden_genii_dir/bes-activities/

  echo "Copying necessary file to Grid namespace:" $RNSPATH
  grid cp local:$PWD/inside-container.sh grid:$RNSPATH

	echo "Logging into admin to create directories"
	getAdminCredentials
	echo "Adding image to Images userX's Image dir"
	grid mkdir -p /home/CCC/Lancium/userX/Images
	grid chmod -R /home/CCC +rwx /users/xsede.org/userX
    dropAdminCredentials
    if [ $singularity_installed -eq 0 ]; then
        echo Singularity not installed!
        echo Skipping singularity one time setup. 
    else
        singularity pull docker://ubuntu:18.04
        grid cp local:$PWD/ubuntu_18.04.sif grid:/home/CCC/Lancium/userX/Images/ubuntu_18.04.sif
    fi

    grid cp local:$PWD/ubuntu18.04.qcow2 grid:/home/CCC/Lancium/userX/Images/ubuntu18.04.qcow2

	echo "Adding local FS Images dir"
	mkdir -p $hidden_genii_dir/bes-activities/Images/userX/
}

testQueueResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}

testSingularityImageJob()
{
    if [ $singularity_installed -eq 0 ]; then
        echo Singularity not installed!
        echo Skipping singularity test!
    else
        submit="$(grid qsub $QUEUE_PATH local:$GENERATED_JSDL_FOLDER/singularity-image-job.jsdl)"
        assertEquals "Submitting singularity image job" 0 $?
        echo "Waiting 15 seconds for job to finish and data to be staged out."
        sleep 15
        grid ls grid:$RNSPATH
        grid cat grid:$RNSPATH/singularity-image-job.out
        hostname_sleep_output="$(grid cat grid:$RNSPATH/singularity-image-job.out)"
        echo $hostname_sleep_output | grep -q "inside container!"
        assertEquals "Image job should complete normally inside the container" 0 $?
    fi
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
	rm -r $hidden_genii_dir/bes-activities/Images
	rm $hidden_genii_dir/bes-activities/singularity-wrapper.sh
	rm $hidden_genii_dir/bes-activities/vmwrapper.sh
    rm ubuntu_18.04.sif
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

