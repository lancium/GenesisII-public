#!/bin/bash

##Author: Vanamala Venkataswamy
#mods: Chris Koeritz

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

  grid rm -rf $RNSPATH/test-bes-attr &>/dev/null

  rm -f status.out
}

testMakingAsyncFolder()
{
  grid mkdir $RNSPATH/test-bes-attr
  assertEquals "Making test-bes-attr directory" 0 $?
  grid cp local:./hostname-sleep.sh grid:$RNSPATH
  assertEquals "Copying datafiles to grid" 0 $?
}

testBesResourcesExist()
{
  available_resources="$(get_BES_resources)"
  assertEquals "Listing $QUEUE_PATH/resources" 0 $?
}


testGetBesAttributes()
{
    for i in $available_resources; do
        grid get-bes-attributes $i
        assertEquals "Get BES activities for factory $i" 0 $?
    done
}

#testGetBesActivities()
#{
#    
#    for i in $available_resources; do
#        grid get-bes-activities $i
#        assertEquals "Get BES activities for factory $i" 0 $?
#    done
#}

testCreateActivity()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
    grid run --async-name=$RNSPATH/test-bes-attr/$shortbes-hostname-sleep-60s --jsdl=local:$GENERATED_JSDL_FOLDER/hostname-sleep-60s.jsdl $i
    assertEquals "Submitting single '/bin/ls' job on $i" 0 $?
  done
}

testGetActivityAttributes()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
    grid get-attributes $RNSPATH/test-bes-attr/$shortbes-hostname-sleep-60s
    assertEquals "Checking activity attributes on $i" 0 $?
  done
}

oneTimeTearDown()
{
  for i in $available_resources; do
    local shortbes="$(basename $i)"
#hmmm: this is only necessary until we fix rm -r, which should carefully unlink resource forks rather than just giving up.
    grid unlink $RNSPATH/test-bes-attr/$shortbes-hostname-sleep-60s 
  done
  grid rm -r $RNSPATH/test-bes-attr 
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

