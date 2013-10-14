#!/bin/bash

# this script performs an automated build.
# it requires two parameters: (1) the directory where the recent GenesisII builds can be found
# and (2) the port to use for running the bootstrapped container.
#
# Author: Chris Koeritz

##############

# get into the real directory for this test.
script_lib_dir="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
export SCRIPT_TOP="$( \cd "$script_lib_dir/.." && \pwd )"  # go to top of hierarchy.
cd $SCRIPT_TOP

# stuff important values into the environment.
#
# this tells the regression test to run some extra tests that don't
# make sense on "real" grids.
export AUTOBUILD_RUNNING=true

# get required parameters...
export BUILDS_FOLDER="$1"; shift
export WORKSPACE_DIR="$1"; shift
# optional parameters...
# using a different port for the container is an option...
export DIFFERENT_PORT="$1"; shift
# optional password we will use for the USERPATH identity.
user_password="$1"; shift
if [ -z "$user_password" ]; then
  user_password=FOOP
fi

if [ -z "$BUILDS_FOLDER" -o -z "$WORKSPACE_DIR" ]; then
  echo This script needs to be passed a builds folder where we can find the latest
  echo genesis2 build, and a workspace directory where the genesis code was built.
  echo Optionally, a port can be provided to run the container on a non-default port.
  exit 1
fi
if [ ! -d "$BUILDS_FOLDER" -o ! -d "$WORKSPACE_DIR" ]; then
  echo The builds folder or workspace directory that were passed do not exist.
  exit 1
fi

# things we can intuit or have hard-coded for now.
export GRITTY_TESTING_TOP_LEVEL="$SCRIPT_TOP"
export TMP="$SCRIPT_TOP"
export GENII_USER_DIR="$SCRIPT_TOP/genesis_user_dir"
export BACKUP_USER_DIR="$SCRIPT_TOP/genesis_secondary_dir"
export GENII_INSTALL_DIR="$SCRIPT_TOP/genesis_build"
export NON_INTERACTIVE=true
if [ -z "$JAVA_HOME" ]; then
  echo The JAVA_HOME variable is not set, and this is required for proper
  echo operation of the bootstrap process.  Please point this variable at
  echo the local Java installation.
  exit 3
fi

##############

function bail_on_fail()
{
  if [ $? -ne 0 ]; then
    echo "Failed previous step.  Now leaving test."
    # shut down any running containers if we can.
    bash $GRITTY_TESTING_TOP_LEVEL/library/zap_genesis_javas.sh
    exit 1
  fi
}

##############

# need to fix up an input file to use for all our testing.

INPUTFILE_FOR_JENKINS=$GRITTY_TESTING_TOP_LEVEL/examples/inputfile.jenkins
if [ "$NAMESPACE" == "xsede" ]; then
  INPUTFILE_FOR_JENKINS=$GRITTY_TESTING_TOP_LEVEL/examples/inputfile.jenkins-xsede
fi

# give the build an input file it can use.  this one relies on our having set
# the crucial variables beforehand in the code above; they will percolate down
# to the bootstrapping scripts and test scripts.
sed -e "s/GENII_INSTALL_DIR=.*/GENII_INSTALL_DIR=\$GENII_INSTALL_DIR/" \
  -e "s/GENII_USER_DIR=.*/GENII_USER_DIR=\$GENII_USER_DIR/" \
  -e "s/BACKUP_USER_DIR=.*/BACKUP_USER_DIR=\$BACKUP_USER_DIR/" \
  < $INPUTFILE_FOR_JENKINS \
  > $GRITTY_TESTING_TOP_LEVEL/inputfile.txt
bail_on_fail

##############

# take out any existing container that's running.  we need to ensure the install folder
# is not slathered with running processes.
bash $GRITTY_TESTING_TOP_LEVEL/library/zap_genesis_javas.sh

# clear out older build packages.
\rm -f "$SCRIPT_TOP/GenesisII-"*.gz

# clear out obnoxious axis file leakage.
\rm -f /tmp/Axis*.att

# clean up any old logging gunk.
\rm -rf $GRITTY_TESTING_TOP_LEVEL/logs
mkdir $GRITTY_TESTING_TOP_LEVEL/logs

# clean up log files left from previous runs.
find $GRITTY_TESTING_TOP_LEVEL -iname "container.log*" -or -iname "grid-client.log*" -exec rm "{}" ';'

# make a directory for running the latest genesis build from.
\rm -rf "$GENII_INSTALL_DIR"
mkdir "$GENII_INSTALL_DIR"
if [ $? -ne 0 ]; then
  echo "Failed to create genesis install dir in $GENII_INSTALL_DIR"
  exit 1
fi

# locate the newest build package.
build_pack=$(find "$BUILDS_FOLDER" -iname "GenesisII-[sb][ou][ui][rl][cd]*.gz" | sort | tail -n 1)
if [ -z "$build_pack" ]; then
  echo "Failed to calculate a build package to run from $BUILDS_FOLDER"
  exit 1
fi

# we're now saving the build package that the tests ran against, so that we have a hope of
# doing a postmortem on a failed build; the code builds go fast, whereas sometimes one might
# want to spelunk on the failed test a while later.  plus this allows us to have a much smaller
# set of code builds archived, since there's already another copy.
local_build_pack="$(\pwd)/$(basename "$build_pack")"
pushd "$GENII_INSTALL_DIR" &>/dev/null
# get that most recent package for genesis.
cp "$build_pack" "$local_build_pack"
if [ $? -ne 0 ]; then
  echo "Failed to copy the genesis build to local space from: $build_pack"
  exit 1
fi
tar -xf "$local_build_pack"
if [ $? -ne 0 ]; then
  echo "Failed to unpack the genesis build from: $local_build_pack"
  exit 1
fi
popd &>/dev/null

# patch all the paths so they point to the current genesis 2 location.
bash $GRITTY_TESTING_TOP_LEVEL/library/genesis2_path_fixer.sh $GENII_INSTALL_DIR $WORKSPACE_DIR

##############

# active part of test begins.

echo "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"
echo "Configured to run grid code from: $GENII_INSTALL_DIR"
echo "User directory is in: $GENII_USER_DIR"
echo "Tests are running from: $GRITTY_TESTING_TOP_LEVEL"
echo "Build package under test is: $build_pack"
echo -e "Java Version info:\n$(java -version 2>&1)"
echo "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"

################

# fix the logging level so we don't get noisy BS in our command output.
# also point the logging directory at our local logs folder instead of at default
# location of ${user.home}/.GenesisII.
for i in $GENII_INSTALL_DIR/lib/genesisII*log4j.properties; do
  sed -i \
    -e "s/log4j.rootCategory=.*, *VCONSOLE, *LOGFILE/log4j.rootCategory=DEBUG, VCONSOLE, LOGFILE/" \
    -e "s%\${user.home}\/.GenesisII%$GRITTY_TESTING_TOP_LEVEL\/logs%" \
      "$i" &>/dev/null
done

################

# get the test environment loaded up.
source $GRITTY_TESTING_TOP_LEVEL/prepare_tests.sh $GRITTY_TESTING_TOP_LEVEL/prepare_tests.sh

source $XSEDE_TEST_ROOT/library/establish_environment.sh

################
# switch over the port used by the container before we start it.
pushd $GENII_INSTALL_DIR &>/dev/null
# if they didn't provide a different port, we will.
if [ -z "$DIFFERENT_PORT" ]; then
  DIFFERENT_PORT=10402
fi
bash change-port.sh $DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME 18080 $DIFFERENT_PORT
  # we don't check for failure here, because a previous bootstrap may have
  # run against the same build, and we won't change those port numbers again.
popd &>/dev/null
################

################
echo Patching runContainer.sh for memory limit...
pushd $GENII_INSTALL_DIR &>/dev/null
sed -i -e "s/-Xmx512M/-Xmx2G/" "runContainer.sh"
chmod 755 "runContainer.sh" 
popd &>/dev/null
################

# clean up the current user directory and prior state.
\rm -rf "$GENII_USER_DIR" "$BACKUP_USER_DIR" \
  "$GENII_INSTALL_DIR/webapps/axis/WEB-INF/attachments"/*

# create a new user directory.
umask 077
mkdir "$GENII_USER_DIR"
bail_on_fail
mkdir "$BACKUP_USER_DIR"
bail_on_fail

# clean out prior test results.  better be getting archived.
echo "Cleaning previous test run."
\rm -rf $TEST_TEMP
echo "Re-establishing temporary directories"
source $GRITTY_TESTING_TOP_LEVEL/prepare_tests.sh

echo "quick-start grid bootstrap commencing..."
bash $XSEDE_TEST_ROOT/library/bootstrap_quick_start.sh
bail_on_fail

# go through the full regression test suite now and see how it does.
echo "Running entire regression test suite."
cd "$XSEDE_TEST_ROOT"
bash regression_test.sh
bail_on_fail

# stop our container again since we're done testing.
echo "Stopping container."
bash $GRITTY_TESTING_TOP_LEVEL/library/zap_genesis_javas.sh

# clear out obnoxious axis file leakage, again.
\rm -f /tmp/Axis*.att "$GENII_INSTALL_DIR/webapps/axis/WEB-INF/attachments"/Axis*.att

# whack files that are too large.
whack_list=$(find $GENII_USER_DIR -size +250M)
echo "Destroying overly large files in derby; possibly due to leftover copies?:"
echo "$whack_list"
\rm -f $whack_list

check_logs_for_errors "$DEPLOYMENT_NAME"
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

echo "Automated regression test finished at $(date)"

# if we get to here, we call that a success.
echo "Totally done with test run, exiting now."
exit 0

