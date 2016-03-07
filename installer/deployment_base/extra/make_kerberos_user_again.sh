#!/bin/bash

# recreates an xsede portal identity for a particular user name; very handy
# for testing small bootstrapped grids.

# either this variable needs to be set before executing the script or it needs to be
# updated within the script to one's admin password.
if [ -z "$ADMIN_PASSWORD" ]; then
  ADMIN_PASSWORD=admin
fi

user="$1"; shift

if [ -z "$user" ]; then
  echo "No user was provided as first parameter, but this is required for recreating"
  echo "the xsede user identity"
  exit 1
fi

$GENII_INSTALL_DIR/grid logout --all ; $GENII_INSTALL_DIR/grid login --username=admin "--password=$ADMIN_PASSWORD"

bash $GENII_INSTALL_DIR/toolkit/tools/migration/recreate_kerb_user.sh $user

#not portable.
cp $GENII_INSTALL_DIR/installer/deployment_base/extra/KHANDROMA.CS.VIRGINIA.EDU@TERAGRID.ORG.gffs-sts.keytab $GENII_INSTALL_DIR/deployments/bootstrapped_grid/security/

$GENII_INSTALL_DIR/grid chmod /groups/xsede.org/gffs-users 0 /users/xsede.org/$user 

$GENII_INSTALL_DIR/grid chmod /groups/xsede.org/gffs-users --pattern='O=National Center for Supercomputing Applications' +rx local:$GENII_INSTALL_DIR/deployments/default/security/myproxy-certs/c36f6349.0

#old: $GENII_INSTALL_DIR/grid chmod /groups/xsede.org/gffs-users --pattern='O=National Center for Supercomputing Applications' +rx local:$GENII_INSTALL_DIR/deployments/default/security/myproxy-certs/f2e89fe3.0

$GENII_INSTALL_DIR/grid logout --all 

echo "testing new user account; please log in."
$GENII_INSTALL_DIR/grid xsedeLogin --username=$user 

#$GENII_INSTALL_DIR/grid whoami --verbosity=HIGH
$GENII_INSTALL_DIR/grid whoami

