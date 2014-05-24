#!/bin/bash

# this script downloads and builds the projects that genesis depends on.
# it is designed to run from the genesis build directory, since it plans
# on storing jars into the ext directory.

# WORKDIR is the directory where this script started out in.
export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
# we're assuming that this script lives in the genesis2 scripts directory.
export TOPDIR="$WORKDIR/.."
pushd "$TOPDIR"

# set up some important variables for the success of the build.
export ANT_OPTS='-Xms512m -Xmx768m -XX:MaxPermSize=768m'

# prints an error message (from parameters) and exits if the previous command failed.
function check_result()
{
  if [ $? -ne 0 ]; then
    echo -e "Step failed: $*"
    exit 1
  fi
}

# state markers fed by the command line.
CLEAN_UP=
TRUNK_BUILD=
#SVN_UPDATE=

# we support some flags on the command line:
#   "clean" requests that we should clean the projects.
#   "wipe" requests that we aggressively clean the projects.
#   "trunk" means that we should expect that this script is in the trunk, and
#      we will create a new subdirectory for the projects.
while true; do
  flag="$1"; shift
  if [ -z "$flag" ]; then break; fi
  if [ "$flag" == "clean" ]; then
    CLEAN_UP=true
  elif [ "$flag" == "trunk" ]; then
    TRUNK_BUILD=true
  elif [ "$flag" == "wipe" ]; then
    # we don't do anything extra beyond clean for wipe.
    CLEAN_UP=true
  else
    false
    check_result "this script cannot use a flag of '$flag'"
  fi
done

# make the storage area if it doesn't exist.
if [ ! -d "$TOPDIR/ext" ]; then mkdir "$TOPDIR/ext"; fi

if [ ! -z "$TRUNK_BUILD" ]; then
  CHECKOUT_DIR="libraries/"
else
  CHECKOUT_DIR=
fi

# loop over all the dependencies we want to have updated for the uber-build...
# first tier is independent libraries, second tier is libs dependent on the
# first tier, etc.
# DPage is last since it's a weird optional package for dynamic server pages.

for subproject in \
\
  CmdLineManipulator FSViewII GeniiJSDL GeniiProcessMgmt MacOSXSwing MNaming \
  GridJobTool gffs-basics \
  gffs-webservices gffs-security \
  gffs-structure \
  DPage \
\
; do 
  echo "=============="
  echo "Building subproject $subproject"
  DIRNAME="$CHECKOUT_DIR$subproject"
  if [ ! -d "$DIRNAME" ]; then mkdir -p "$DIRNAME"; fi
  check_result "making dependency folder $DIRNAME"
  pushd "$DIRNAME"
  check_result "entering folder $DIRNAME"
  cd trunk
  check_result "entering trunk for $subproject"

  if [ ! -z "$CLEAN_UP" ]; then
    ant clean
    check_result "ant clean for $subproject"
  else
    ant build
    retval=$?
    if [ $retval -ne 0 -a $subproject == gffs-security ]; then
      echo -e "\n======="
      echo "Failures in gffs-security often result from not having the unlimited JCE jar"
      echo "files installed in the jre/lib/security folder.  These are available at:"
      echo "http://www.oracle.com/technetwork/java/javase/downloads/index.html"
      echo
      echo "Often the command that will correct this build problem is:"
      echo "sudo cp $GENII_INSTALL_DIR/installer/unlimited_jce_java7/*jar /usr/lib/jvm/java-7-oracle/jre/lib/security/"
      echo
      echo -e "=======\n"
    fi
    # re-enact the retval...
    if [ $retval -ne 0 ]; then false; else true; fi
    check_result "ant build for $subproject"
    # publish the newly crafted jars into the main build's ext folder.
    cp -v -f lib/*.jar "$TOPDIR/ext"
    check_result "publishing jar file produced by $subproject"
  fi

  popd
  echo "SUCCESS for subproject $subproject"
  echo "=============="
  echo
done

if [ ! -z "$TRUNK_BUILD" -a -z "$CLEAN_UP" ]; then
  # grab the services in the web services jar.
  pushd deployments/default
  check_result "entering default deployment"
  \rm -rf services
  check_result "cleaning any existing services"
  unzip ../../$CHECKOUT_DIR/gffs-webservices/trunk/lib/gffs-webservices.jar services/*
  check_result "unpacking services directory from gffs-webservices.jar"
  popd
fi

# done now, so just pop outermost directory.
popd

