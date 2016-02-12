#!/bin/bash

# Processes the XSEDE tools config file to turn variables listed in the
# file into exported variables in the environment.
#
# Author: Chris Koeritz

##############

# this processes the single file of input parameters at the test root and
# turns it into a collection of environment variables.  we then load all those
# variables into the current environment.  we will also automatically fill in
# some important variables here that we'll use later.  in some cases, we will
# use the existing value if the variable is already set.
define_and_export_variables()
{
  if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; return 3; fi

  # create our output folder so we can store logs and temporaries.
  mkdir -p "$TEST_TEMP" &>/dev/null

  # on the off-chance that this is actually a local path, and we have control over
  # that path, go ahead and create it.  if it's a remote path in a different user's
  # directory that's intended for the actual container, then this won't succeed, but
  # we don't care.  in that case it is up to the container's owner to have the path
  # for us, and for us to ask for it...
  if [ ! -z "$EXPORTPATH" ]; then
    mkdir -p "$EXPORTPATH" &>/dev/null
  fi

  # start writing the environment file.
  echo > $TEST_TEMP/env_file
  # turn each useful line in input file into an environment variable declaration.
  while read line; do
    # match lines that are comments or blank.
    echo "$line" | grep -e '^[#;]' -e '^[ 	]*$' &>/dev/null
    # only export non-useless lines.
    if [ $? != 0 ]; then
      echo "$line" | grep '[a-z0-9A-Z]=(' &>/dev/null
      if [ $? == 0 ]; then
        # it's an array variable so don't try to export it or bash loses it for us.
        echo $line >> $TEST_TEMP/env_file
      else
        echo "export" $line >> $TEST_TEMP/env_file
      fi
    fi
  done < "$GFFS_TOOLKIT_CONFIG_FILE"

  # now run the environment file to add those settings to our environment.
  chmod +x $TEST_TEMP/env_file
  source $TEST_TEMP/env_file &>/dev/null

  if [ -z "$FOLDERSPACE" ]; then
    echo "No FOLDERSPACE variable was defined; using 'xsede.org' as default."
    export FOLDERSPACE="xsede.org"
  fi

  # this switchboard sets up the variables we will want to use for storage locations.
  export BOOTSTRAP_LOC=/resources/$FOLDERSPACE/containers/BootstrapContainer
  if [ "$FOLDERSPACE" == "xsede.org" ]; then
     export STS_LOC=/resources/$FOLDERSPACE/containers/sts-1.xsede.org
  else
     export STS_LOC=$BOOTSTRAP_LOC
  fi
  export CONTAINERS_LOC=/resources/$FOLDERSPACE/containers
  export USERS_LOC=/users/$FOLDERSPACE
  export HOMES_LOC=/home/$FOLDERSPACE
  export BES_CONTAINERS_LOC=/resources/$FOLDERSPACE/containers
  export GROUPS_LOC=/groups/$FOLDERSPACE
  export QUEUES_LOC=/resources/$FOLDERSPACE/queues

  # define the location of our main binaries / scripts.  this formerly was the
  # same as GENII_INSTALL_DIR but is now moved down into a bin directory.
  export GENII_BINARY_DIR=$GENII_INSTALL_DIR

  if [ -z "$GENII_USER_DIR" ]; then
    export GENII_USER_DIR="$HOME/.genesisII-2.0"
    echo "GENII_USER_DIR was not defined; using default of: $GENII_USER_DIR"
  fi

  # calculate the deployments directory if there's an override.
  if [ -e "$GENII_INSTALL_DIR/lib/container.properties" ]; then
    export DEPLOYMENTS_ROOT="$(sed -n -e 's/^edu.virginia.vcgr.genii.container.deployment-directory=\(.*\)/\1/p' < "$GENII_INSTALL_DIR/lib/container.properties")"
#    echo "calculated deployments root as: $DEPLOYMENTS_ROOT"
  fi

  if [ -z "$DEPLOYMENTS_ROOT" ]; then
    export DEPLOYMENTS_ROOT="$GENII_INSTALL_DIR/deployments"
  fi
  if [ -z "$ACTUAL_DEPLOYMENT_FOLDER" ]; then
    export ACTUAL_DEPLOYMENT_FOLDER="$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME"
  fi
  if [ -z "$NAMESPACE_FILE" ]; then
    export NAMESPACE_FILE=$ACTUAL_DEPLOYMENT_FOLDER/configuration/namespace.properties
  fi
  if [ -z "$BOOTSTRAP_FILE" ]; then
    export BOOTSTRAP_FILE="$ACTUAL_DEPLOYMENT_FOLDER/configuration/bootstrap.xml"
  fi

  ##############

  # we will not do the file existence check if there's a chance that the code
  # has not been built yet, and the caller knows this and tells us.
  if [ -z "$POSSIBLY_UNBUILT" ]; then
    # make sure we like the xsede install folder...
    if [ ! -f "$GENII_BINARY_DIR/grid" -a ! -f "$GENII_BINARY_DIR/grid.exe" -a ! -f "$GENII_BINARY_DIR/grid.bat" ]; then
      echo "The GenesisII client program ('grid') is missing in the directory specified"
      echo "by the GENII_INSTALL_DIR environment variable, which is currently defined as:"
      echo "  '$GENII_INSTALL_DIR'"
      echo "Please ensure that GenesisII is installed and that the GENII_INSTALL_DIR in"
      echo "  '$GFFS_TOOLKIT_CONFIG_FILE'"
      echo "is correctly defined for the installation path."
      return 1
    fi
  fi
}

