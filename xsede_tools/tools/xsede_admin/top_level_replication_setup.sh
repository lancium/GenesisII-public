#!bin/bash

replica_host="$1"; shift
replica_port="$1"; shift

function print_instructions() {
  echo -e "\
This script performs the top-level replication steps on the XSEDE grid.\n\
It will not succeed if it has already been run before.\n\
Note that the script is designed to be run on an isolated client, not\n\
directly on the root or root replica container.\n\
\n\
The script takes the following parameters:\n\
1) The hostname of the root replica container, where the root container's\n\
   resolver and resource replicas will be stored.\n\
2) The port number where the root replica is listening for requests.\n\
\n\
This script requires that the following environment variables are set:\n\
GENII_INSTALL_DIR - the location of the Genesis II GFFS client that is\n\
    configured to point at the root container.\n\
GENII_USER_DIR - the location where the state directory for Genesis is\n\
    located.\n\
\n\
"
}

# Define the mirror container path variable.
export MIRRORPATH=/resources/xsede.org/containers/gffs-2.xsede.org

if [ -z "$replica_host" -o -z "$replica_port" ]; then
  print_instructions
  echo "*This script requires the hostname and port number where the root replica"
  echo "*container is running."
  exit 1
fi
if [ -z "$GENII_INSTALL_DIR" -o -z "$GENII_USER_DIR" ]; then
  print_instructions
  echo "*This script requires that the GENII_INSTALL_DIR and GENII_USER_DIR variables"
  echo "*be established before running it."
  exit 1
fi

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"
#export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../../prepare_tools.sh ../../prepare_tools.sh 
fi

#source "$XSEDE_TEST_ROOT/library/establish_environment.sh"
source "$XSEDE_TEST_ROOT/library/helper_methods.sh"

# The mirror container needs to be linked in at the MIRRORPATH:
$GENII_INSTALL_DIR/grid ln \
  --service-url=https://${replica_host}:${replica_port}/axis/services/VCGRContainerPortType \
  $MIRRORPATH 
check_if_failed "Linking replica container into place at $MIRRORPATH"

MIRROR_CHK_FILE="$(mktemp $TEST_TEMP/mirror-container-listing.XXXXXX)"
$GENII_INSTALL_DIR/grid ls $MIRRORPATH >"$MIRROR_CHK_FILE"
check_if_failed "Listing contents under $MIRRORPATH"
grep "Services" "$MIRROR_CHK_FILE"
check_if_failed "Testing container at $MIRRORPATH; container does not seem to be running!"
rm "$MIRROR_CHK_FILE"

# Give access to the rootResolver to everyone.
$GENII_INSTALL_DIR/grid chmod $MIRRORPATH/Services/GeniiResolverPortType \
  +rx --everyone
check_if_failed "Giving everyone permission to the resolver port type"

# Create a resolver on the secondary container:
$GENII_INSTALL_DIR/grid create-resource \
  $MIRRORPATH/Services/GeniiResolverPortType \
  /etc/resolvers/rootResolver 
check_if_failed "Creating root resolver"

# Give everyone permissions to use the resolver 
# for finding replicated files & directories.
$GENII_INSTALL_DIR/grid chmod /etc/resolvers/rootResolver +rx --everyone
check_if_failed "Giving everyone permission on root resolver"
# Add a resolver for the root folder in the namespace:
$GENII_INSTALL_DIR/grid resolver / /etc/resolvers/rootResolver
check_if_failed "Adding a resolver to root of RNS"

# Test that the resolver was added properly:
RESOLVER_TMP_FILE="$(mktemp $TEST_TEMP/gffs-resolver-output.XXXXXX)"
$GENII_INSTALL_DIR/grid resolver -q / >"$RESOLVER_TMP_FILE"
check_if_failed "Testing resolver for root"
echo Resolver information for root:
cat "$RESOLVER_TMP_FILE"
# …the above command should print out 4-5 lines of resolver information.  If it 
# does not, you may need to start over with a clean GENII_USER_DIR and with only the
# appropriate keystoreLogin commands.
if [ ! -s "$RESOLVER_TMP_FILE" ]; then
  echo "The addition of the resolver to the root failed.  This failure may require"
  echo "restoring the root container from backup in order to try again."
  exit 1
fi
rm "$RESOLVER_TMP_FILE"

# Register the top-level folders with the resolver.  This is a suggested
# set for the XSEDE namespace definition.  Other folders can also be
# added to the resolver if needed. 

$GENII_INSTALL_DIR/grid <<eof
resolver /etc /etc/resolvers/rootResolver 
resolver /etc/resolvers /etc/resolvers/rootResolver 
resolver /resources /etc/resolvers/rootResolver 
resolver /resources/xsede.org /etc/resolvers/rootResolver 
resolver /resources/xsede.org/containers /etc/resolvers/rootResolver 
resolver /resources/xsede.org/queues /etc/resolvers/rootResolver 
resolver /groups /etc/resolvers/rootResolver 
resolver /groups/xsede.org /etc/resolvers/rootResolver
resolver /home /etc/resolvers/rootResolver 
resolver /home/xsede.org /etc/resolvers/rootResolver 
resolver /users /etc/resolvers/rootResolver
resolver /users/xsede.org /etc/resolvers/rootResolver 
eof
check_if_failed "Adding resolvers for chosen top-level and second-level directories"
# Replicates the rest of the top-level folders, except /etc/resolvers.
$GENII_INSTALL_DIR/grid <<eof
replicate / $MIRRORPATH
replicate /etc $MIRRORPATH
replicate /resources/xsede.org/containers $MIRRORPATH 
replicate /resources/xsede.org/queues $MIRRORPATH
replicate /resources/xsede.org $MIRRORPATH
replicate /resources $MIRRORPATH
replicate /groups $MIRRORPATH
replicate /groups/xsede.org $MIRRORPATH
replicate /home $MIRRORPATH
replicate /home/xsede.org $MIRRORPATH
replicate /users $MIRRORPATH
replicate /users/xsede.org $MIRRORPATH
eof
check_if_failed "Adding replicas for chosen top-level and second-level directories"
# Replicate just the groups that live on the root container:
$GENII_INSTALL_DIR/grid ls /groups/xsede.org \
  | tail -n +2 >$HOME/group_list
while read line; do \
  if [ ! -z "$line" ]; then \
    $GENII_INSTALL_DIR/grid resolver /groups/xsede.org/$line \
      /etc/resolvers/rootResolver; \
    check_if_failed "Adding a resolver for group $line"
    $GENII_INSTALL_DIR/grid replicate /groups/xsede.org/$line \
      /resources/xsede.org/containers/gffs-2.xsede.org; \
    check_if_failed "Adding a replica for group $line"
  fi; \
done < $HOME/group_list
# Save a new context.xml containing the resolver listing (crucial):
$GENII_INSTALL_DIR/grid logout --all
$GENII_INSTALL_DIR/grid cd /
check_if_failed "Changing to root of RNS"
cp $GENII_USER_DIR/user-context.xml $HOME/replicated-context.xml
check_if_failed "Copying new replicated context file into home directory"

echo "There should now be a file called 'replicated-context.xml' in your home"
echo "directory.  This file should be conveyed to the Genesis II team so that"
echo "they can build a new set of installers that include it; please send the"
echo "file to xcghelp@cs.virginia.edu with a request for the installer to be"
echo "updated to use it."

