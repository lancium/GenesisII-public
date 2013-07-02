#!/bin/bash

# this script downloads and builds the projects that genesis depends on.
# it is designed to run from the genesis build directory, since it plans
# on storing jars into the ext directory.

# the svn repository at the root of the libraries.
#LIBRARY_REPO="svn://svn.xcg.virginia.edu:9002/GENREPO/libraries"
LIBRARY_REPO="svn://svn.xcg.virginia.edu:9002/GENREPO/GenesisII/branches/cak0l/libraries"

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
SVN_UPDATE=

# we support some flags on the command line:
#   "clean" requests that we should clean the projects.
#   "update" says that it's okay to do an svn update to get the source code.
#      otherwise checkouts must already exist.
#   "trunk" means that we should expect that this script is in the trunk, and
#      we will create a new subdirectory for the projects.
while true; do
  flag="$1"; shift
  if [ -z "$flag" ]; then break; fi
  if [ "$flag" == "clean" ]; then
    CLEAN_UP=true
  elif [ "$flag" == "trunk" ]; then
    TRUNK_BUILD=true
  elif [ "$flag" == "update" ]; then
    SVN_UPDATE=true
  else
    false
    check_result "this script cannot use a flag of '$flag'"
  fi
done

# make the storage area if it doesn't exist.
if [ ! -d "$TOPDIR/ext" ]; then mkdir "$TOPDIR/ext"; fi

if [ ! -z "$TRUNK_BUILD" ]; then
  CHECKOUT_DIR="subprojects/"
else
  CHECKOUT_DIR=
fi

# given a project name, we load it up with the jars it needs.
function snag_dependencies()
{
  local projname="$1"; shift
  # ugly dependency management section.
  if [ $projname == gffs-basics ]; then
    cp -v -f $TOPDIR/ext/app-manager.jar ./ext
  fi
  if [ $projname == GridJobTool ]; then
    cp -v -f $TOPDIR/ext/GeniiJSDL.jar ./ext
  fi
  if [ $projname == gffs-webservices ]; then
    cp -v -f $TOPDIR/ext/gffs-basics.jar ./ext
  fi
  if [ $projname == gffs-security ]; then
    cp -v -f $TOPDIR/ext/app-manager.jar $TOPDIR/ext/gffs-basics.jar ./ext
  fi
  if [ $projname == gffs-structure ]; then
    cp -v -f $TOPDIR/ext/gffs-basics.jar $TOPDIR/ext/gffs-webservices.jar $TOPDIR/ext/gffs-security.jar $TOPDIR/ext/app-manager.jar $TOPDIR/ext/CmdLineManipulator.jar $TOPDIR/ext/fsview.jar $TOPDIR/ext/GeniiJSDL.jar $TOPDIR/ext/GeniiProcMgmt.jar $TOPDIR/ext/GridJobTool.jar ./ext
  fi
  check_result "copying dependent jars for subproject $projname"
}

# loop over all the dependencies we want to have updated for the uber-build...
# first tier is independent libraries, second tier is libs dependent on the
# first tier, etc.
# DPage is last since it's a weird optional package for dynamic server pages.

for subproject in \
\
  ApplicationManager CmdLineManipulator FSViewII GeniiJSDL GeniiProcessMgmt MacOSXSwing MNaming \
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
  if [ ! -d "trunk" ]; then
    if [ -z "$SVN_UPDATE" -a -z "$CLEAN_UP" ]; then
      false
      check_result "failing because there is no existing checkout folder '$DIRNAME'\nand update was not requested."
    fi
    svn co "$LIBRARY_REPO/$subproject/trunk"
  else
    if [ ! -z "$SVN_UPDATE" ]; then
      svn up trunk
    fi
  fi
  check_result "checking out subproject $subproject"
  cd trunk
  check_result "entering trunk for $subproject"

  if [ ! -z "$CLEAN_UP" ]; then
    ant clean
    check_result "ant clean for $subproject"
  else
    # copy in the jars that we depend on.
    snag_dependencies "$subproject"
    ant build
    retval=$?
    if [ $retval -ne 0 -a $subproject == gffs-security ]; then
      echo -e "\n======="
      echo "Failures in gffs-security often result from not having the unlimited JCE jar"
      echo "files installed in the jre/lib/security folder.  These are available at:"
      echo "http://www.oracle.com/technetwork/java/javase/downloads/index.html"
      echo -e "=======\n"
    fi
    # re-enact the retval...
    if [ $retval -ne 0 ]; then false; else true; fi
    check_result "ant build for $subproject"
    # publish the newly crafted jars into the main build's ext folder.
    cp -v -f lib/*.jar $TOPDIR/ext
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

