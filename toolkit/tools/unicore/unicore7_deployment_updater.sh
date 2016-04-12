#!/bin/bash

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo "This script requires that the GENII_INSTALL_DIR has already been set."
  echo "Use 'source ~/GenesisII/set_gffs_vars' in a bash shell to load the"
  echo "GFFS environment before calling this script."
fi

# set the deployment name for xsede.
DEPNAME="current_grid"

pushd $GENII_INSTALL_DIR/deployments

echo
echo "Updating default deployment for UNICORE in GFFS installed at $(pwd)"
echo

cp $DEPNAME/configuration/namespace.properties default/configuration
cp $DEPNAME/configuration/security.properties default/configuration
cp $DEPNAME/configuration/server-config.xml default/configuration
cp $DEPNAME/configuration/web-container.properties default/configuration

cp $DEPNAME/security/trusted.pfx default/security/
cp $DEPNAME/security/grid-certificates/* default/security/grid-certificates/
cp $DEPNAME/security/trusted-certificates/* default/security/trusted-certificates/

popd

echo "GFFS default deployment is now ready for UNICORE"
echo


