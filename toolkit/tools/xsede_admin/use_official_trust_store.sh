#!/bin/bash

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo "GENII_INSTALL_DIR variable is not established yet.  This should point at the"
  echo "Genesis II installation folder."
  exit 1
fi

if [ ! -d "$GENII_INSTALL_DIR/deployments/current_grid/configuration" ]; then
  echo "The installation is not using the 'current_grid' deployment.  This script"
  echo "may not be appropriate for this type of installation."
  exit 1
fi

pushd "$GENII_INSTALL_DIR/deployments/current_grid/configuration" &>/dev/null

cat security.properties \
  | sed -e s/edu.virginia.vcgr.genii.client.security.ssl.myproxy-certificates.location=myproxy-certs/"edu.virginia.vcgr.genii.client.security.ssl.myproxy-certificates.location=\/etc\/grid-security\/certificates"/  \
  | sed -e s/edu.virginia.vcgr.genii.client.security.ssl.grid-certificates.location=grid-certificates/"edu.virginia.vcgr.genii.client.security.ssl.grid-certificates.location=\/etc\/grid-security\/certificates"/  \
  > security.properties.modified

echo "Changes made to security.properties:"
diff security.properties security.properties.modified
mv security.properties security.properties.default
mv security.properties.modified security.properties

popd &>/dev/null

