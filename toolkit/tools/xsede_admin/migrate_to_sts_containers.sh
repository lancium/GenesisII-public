#!bin/bash

sts1_host="$1"; shift
sts1_port="$1"; shift
sts2_host="$1"; shift
sts2_port="$1"; shift

function print_instructions() {
  echo -e "\
This script sets up the two STS containers for XSEDE that will take over\n\
authentication duties for the grid.\n\
\n\
The script takes the following parameters:\n\
1) hostname of sts-1 container.\n\
2) port of sts-1 container.\n\
3) hostname of sts-2 container.\n\
4) port of sts-2 container.\n\
\n\
This script requires that the following environment variables are set:\n\
GENII_INSTALL_DIR - the location of the Genesis II GFFS client that is\n\
    configured to point at the root container.\n\
GENII_USER_DIR - the location where the state directory for Genesis is\n\
    located.\n\
\n\
"
}

if [ -z "$sts1_host" -o -z "$sts1_port" -o -z "$sts2_host" -o -z "$sts2_port" ]; then
  print_instructions
  echo "*This script requires the hostname and port number for each of the STS"
  echo "*containers."
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
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../../prepare_tools.sh ../../prepare_tools.sh 
fi

#source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"
source "$GFFS_TOOLKIT_ROOT/library/helper_methods.sh"

echo
echo "primary sts is on '$sts1_host' at port '$sts1_port'"
echo "secondary sts is on '$sts2_host' at port '$sts2_port'"
echo

export STS1PATH=/resources/xsede.org/containers/sts-1.xsede.org
export STS2PATH=/resources/xsede.org/containers/sts-2.xsede.org

echo unlinking the placeholder for sts-1.

# if for some reason the original sts-1 has already been unlinked,
# then try adding the original link back in place with:
# "$GENII_INSTALL_DIR/bin/grid" ln /resources/xsede.org/containers/gffs-1.xsede.org  /resources/xsede.org/containers/sts-1.xsede.org
"$GENII_INSTALL_DIR/bin/grid" unlink $STS1PATH
check_if_failed "Failed to unlink the sts-1 path at: $STS1PATH"

####

echo linking the primary sts into the grid.

"$GENII_INSTALL_DIR/bin/grid" ln \
  --service-url=https://$sts1_host:$sts1_port/axis/services/VCGRContainerPortType \
  $STS1PATH
check_if_failed "Failed to link the sts-1 path to container: $sts1_host"

# test the sts1 path to make sure it's a live container.
STS_CHK_FILE="$(mktemp $TEST_TEMP/sts-container-listing.XXXXXX)"
"$GENII_INSTALL_DIR/bin/grid" ls $STS1PATH >"$STS_CHK_FILE"
check_if_failed "Listing contents under $STS1PATH"
grep -q "Services" "$STS_CHK_FILE"
check_if_failed "Testing container at $STS1PATH; container does not seem to be running!"
rm "$STS_CHK_FILE"

echo linking the secondary sts into the grid.

"$GENII_INSTALL_DIR/bin/grid" ln \
  --service-url=https://$sts2_host:$sts2_port/axis/services/VCGRContainerPortType \
  $STS2PATH
check_if_failed "Failed to link the sts-2 path to container: $sts2_host"

# test the sts2 path to make sure it's a live container.
STS_CHK_FILE="$(mktemp $TEST_TEMP/sts-container-listing.XXXXXX)"
"$GENII_INSTALL_DIR/bin/grid" ls $STS2PATH >"$STS_CHK_FILE"
check_if_failed "Listing contents under $STS2PATH"
grep -q "Services" "$STS_CHK_FILE"
check_if_failed "Testing container at $STS2PATH; container does not seem to be running!"
rm "$STS_CHK_FILE"

# enable permissions on the port types for authentication.

echo setting permissions on primary sts.

"$GENII_INSTALL_DIR/bin/grid" chmod $STS1PATH/Services/X509AuthnPortType \
  +rx --everyone
check_if_failed "Failed to chmod X509AuthnPortType on $STS1PATH"
"$GENII_INSTALL_DIR/bin/grid" chmod $STS1PATH/Services/KerbAuthnPortType \
  +rx --everyone
check_if_failed "Failed to chmod KerbAuthnPortType on $STS1PATH"
"$GENII_INSTALL_DIR/bin/grid" chmod \
  $STS1PATH/Services/EnhancedNotificationBrokerFactoryPortType +rx --everyone
check_if_failed "Failed to chmod notification broker on $STS1PATH"

echo setting permissions on secondary sts.

# Repeated for secondary STS container:
"$GENII_INSTALL_DIR/bin/grid" chmod $STS2PATH/Services/X509AuthnPortType \
  +rx --everyone
check_if_failed "Failed to chmod X509AuthnPortType on $STS2PATH"
"$GENII_INSTALL_DIR/bin/grid" chmod $STS2PATH/Services/KerbAuthnPortType \
  +rx --everyone
check_if_failed "Failed to chmod KerbAuthnPortType on $STS2PATH"
"$GENII_INSTALL_DIR/bin/grid" chmod \
  $STS2PATH/Services/EnhancedNotificationBrokerFactoryPortType +rx --everyone
check_if_failed "Failed to chmod notification broker on $STS2PATH"

echo creating resolver on secondary sts for primary to use.

# create the resolver for STS entries on the secondary STS container.
"$GENII_INSTALL_DIR/bin/grid" create-resource \
  $STS2PATH/Services/GeniiResolverPortType \
  /etc/resolvers/stsResolver
check_if_failed "Failed to create an STS resolver on $STS2PATH"

# provide access to the resolver.
"$GENII_INSTALL_DIR/bin/grid" chmod /etc/resolvers/stsResolver +rx --everyone
check_if_failed "Failed to chmod the STS resolver for use by everyone"

# seems like everything worked.
echo
echo "The STS1 and STS2 containers have been linked into the grid and have"
echo "been configured to take over authentication duties for new accounts."
echo

