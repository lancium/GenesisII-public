#!/bin/bash

# security and certificate related methods.
#
# Author: Chris Koeritz

##############

# define the cert-tool appropriately for the platform.

CERTO="bash $GENII_INSTALL_DIR/cert-tool"
if [ "$OS" == "Windows_NT" ]; then
  CERTO="cmd /c $(echo $GENII_INSTALL_DIR | tr / \\\\)\\cert-tool.bat"
fi

##############

# certificate methods.

# calculates the appropriate DN string for a collection of cert parts.
# this requires one parameter for the CN name.
function calculate_DN()
{
  local cn="$1"; shift
  # make sure we don't get an empty one.
  if [ -z "$cn" ]; then
    cn="GenesisII Certificate"
  fi
  local DN_STRING=""
  if [ ! -z "$C" ]; then
    DN_STRING+="C=$C, "
  fi
  if [ ! -z "$ST" ]; then
    DN_STRING+="ST=$ST, "
  fi
  if [ ! -z "$L" ]; then
    DN_STRING+="L=$L, "
  fi
  if [ ! -z "$O" ]; then
    DN_STRING+="O=$O, "
  fi
  if [ ! -z "$OU" ]; then
    DN_STRING+="OU=$OU, "
  fi
  # last one we put in is the CN.
  DN_STRING+="CN=$cn"
  echo $DN_STRING
}

# requires three parameters, the pfx file to create, the password for it, and the
# alias to use for the certificate inside the pfx.
function create_bootstrap_signing_certificate()
{
  local CA_PFX="$1"; shift
  local CA_PASSWORD="$1"; shift
  local CA_ALIAS="$1"; shift

  local UBER_CA_PFX="$CA_PFX-base.pfx"
  local UBER_CA_ALIAS="base-key"
  run_any_command $CERTO gen -dn="'C=US, ST=Virginia, L=Charlottesville, O=GENIITEST, OU=Genesis II, CN=skynet'" -output-storetype=PKCS12 "-output-entry-pass='$CA_PASSWORD'" -output-keystore=$UBER_CA_PFX "-output-keystore-pass='$CA_PASSWORD'" "-output-alias='$UBER_CA_ALIAS'" -keysize=2048
  check_if_failed "generating base of CA keypair"

  # now create the real signing certificate, with full CA apparel.
  create_certificate_using_CA "$UBER_CA_PFX" "$CA_PASSWORD" "$UBER_CA_ALIAS" "$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "skynet"
  check_if_failed "generating signing keypair"
}

# folds all the certificates into a trusted.pfx file.  needs a directory location.
function create_bootstrap_trusted_pfx()
{
  local dirname="$1"; shift
  for certfile in $dirname/*.cer; do
    local output_alias=$(basename "$certfile" .cer)
    echo -e "Adding '$(basename $certfile)' to store with alias: $output_alias"
    run_any_command $CERTO import "-output-keystore='$dirname/trusted.pfx'" -output-keystore-pass=trusted "-base64-cert-file='$certfile'" "-output-alias='$output_alias'"
    check_if_failed "adding certificate for $certfile"
  done
}

# creates a new certificate based on an existing CA in pkcs12 format.
# 1) pfx file to use as CA, 2) password for CA pfx, 3) alias for the ca key-pair,
# 4) pfx file to generate, 5) password for new pfx, 6) alias for new key-pair,
# 7) common name entry for new key-pair (CN).
function create_certificate_using_CA()
{
  if [ $# -lt 7 ]; then
    echo "create_certificate_using_CA needs 7 parameters: the pfx file with the CA keypair,"
    echo "the password for that file, the alias for the CA pfx, the new pfx file"
    echo "to generate using the CA, the password for the new pfx containing the new"
    echo "keypair, the alias for the new pfx, and the CN (common name) entry for the"
    echo "new key-pair."
    return 1
  fi
  local THE_CA_PFX="$1"; shift
  local THE_CA_PASS="$1"; shift
  local THE_CA_ALIAS="$1"; shift
  local NEW_PFX="$1"; shift
  local NEW_PASS="$1"; shift
  local NEW_ALIAS="$1"; shift
  local CN_GIVEN="$1"; shift

  # first generate the private and public key into the pkcs12 archive.
  local dn="$(calculate_DN "$CN_GIVEN")"
  echo -e "Creating $(basename $NEW_PFX) with alias $NEW_ALIAS and certificate DN:\n    $dn"
  run_any_command $CERTO gen "'-dn=$dn'" -output-storetype=PKCS12 "-output-entry-pass='$NEW_PASS'" "-output-keystore=$NEW_PFX" "-output-keystore-pass='$NEW_PASS'" "-output-alias='$NEW_ALIAS'" "-input-keystore=$THE_CA_PFX" "-input-keystore-pass='$THE_CA_PASS'" -input-storetype=PKCS12 "-input-entry-pass='$THE_CA_PASS'" "-input-alias='$THE_CA_ALIAS'" -keysize=2048
  check_if_failed "generating $NEW_PFX from $THE_CA_PFX"
  # and create its certificate file.
#  local cert_file="$(echo $NEW_PFX | sed -e 's/\.pfx/\.cer/')"
  local cert_file="$(dirname $NEW_PFX)/$(basename $NEW_PFX ".pfx").cer"
  run_any_command $JAVA_HOME/bin/keytool -export -file "$cert_file" -keystore "$NEW_PFX" "-storepass '$NEW_PASS'" "-alias '$NEW_ALIAS'" -storetype PKCS12
  check_if_failed "generating certificate file $cert_file for $NEW_PFX"
}

##############

function create_grid_certificates()
{
  pushd $GENII_INSTALL_DIR &>/dev/null

  local SECURITY_DIR="$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/security"

  local SIGNING_PFX="$SECURITY_DIR/signing-cert.pfx"
  local SIGNING_ALIAS="signing-cert"
  local SIGNING_PASSWD="signer"

  local ADMIN_PFX="$SECURITY_DIR/admin.pfx"
  local ADMIN_CER="$SECURITY_DIR/admin.cer"
  local ADMIN_PASSWD="$ADMIN_ACCOUNT_PASSWD"

  local OWNER_CER="$SECURITY_DIR/owner.cer"

  # clean up any existing certificates.
  \rm -f $SECURITY_DIR/*.pfx $SECURITY_DIR/*.cer

  # fix the patch certificate, which doesn't exist yet, and the app-url.
  sed -i -e 's/^\(edu.virginia.vcgr.appwatcher.patch-signer-certificate.0=.*\)/#\1/' -e 's/^\(edu.virginia.vcgr.appwatcher.application-url.0=.*\)/#\1/' ext/genii-base-application.properties
  check_if_failed "patching application properties for bootstrap"

  # create the root signing certificate.  should be an actual CA now.
  create_bootstrap_signing_certificate "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS"
  check_if_failed "creating a signing certificate for bootstrap"

  # create the admin certificate.
  create_certificate_using_CA "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS" $ADMIN_PFX "$ADMIN_PASSWD" skynet "skynet admin"
  check_if_failed "creating skynet admin certificate using CA"
  cp "$ADMIN_CER" "$SECURITY_DIR/default-owners/admin.cer"
  check_if_failed "copying admin certificate into default-owners"
  cp "$ADMIN_CER" "$OWNER_CER"
  check_if_failed "copying admin certificate as owner cert"
  cp "$ADMIN_CER" "$SECURITY_DIR/default-owners/owner.cer"
  check_if_failed "copying admin certificate as owner.cer in default-owners"

  create_certificate_using_CA "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS" $SECURITY_DIR/tls-cert.pfx tilly tls-cert "TLS certificate"
  check_if_failed "creating TLS certificate using CA"

  create_bootstrap_trusted_pfx $SECURITY_DIR

  popd &>/dev/null
}

##############

# acquires super-user access on this grid.  this is not a normal user; it's based on the
# admin keystore that is assumed to still exist in the deployment.  more useful for test
# grids, unless one is okay with leaving keystores lying around.
function get_root_privileges()
{
  # login with rights to do everything we need.
  echo "Acquiring admin keystore superpowers..."

#hmmm: get the password magic below into some functional method.
  local adminpass=keys
  if [ ! -z "$ADMIN_ACCOUNT_PASSWD" ]; then
    adminpass="$ADMIN_ACCOUNT_PASSWD"
  fi

  grid_chk keystoreLogin "--password=$adminpass" local:$DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/security/admin.pfx
}

##############

# these are the steps that make a user or group (rns path) into a real powerhouse on the grid.
# this requires that the user calling this be logged in as an administrator on the container
# specified, as well as being a grid administrator.
# the first parameter is the user to be given privileges.  the second parameter is the specific
# container being processed.  if there is a third non-empty parameter, then the user will
# be given admin rights on all queues in the grid (not just this container).  if there is a
# non-empty fourth parameter, then the user will also be given rights to all BES in the grid.
# a non-empty fifth parameter tells the function to try to make the user a member of the
# admins and user management groups (if defined for the namespace); only to be used once.
function give_administrative_privileges()
{
  local userpath="$1"; shift
  local container="$1"; shift
  local queue_perms="$1"; shift
  local bes_perms="$1"; shift
  local admin_group="$1"; shift
  if [ -z "$userpath" -o -z "$container" ]; then
    echo "Error in give_administrative_privileges function--need to specify user path and"
    echo "container name."
    exit 1
  fi
  if [ "$(basename $container)" == "$container" ]; then
    # they gave us the raw container name, so assume they're talking about the default
    # location for a container.
    container="$CONTAINERS_LOC/$container"
  fi
  echo "Giving '$userpath' administrative rights across '$container'..."
  # this set of commands makes the rns path into an admin, mainly on the container.
  # the bes for the container is assumed to be created by us, so one may have to add
  # perms manually if it is not in "$BES_CONTAINERS_LOC/{shortContainerName}-bes".

  # this is extra for the xsede namespace, so we can attain gffs-admins rights.
  if [ ! -z "$admin_group" -a "$NAMESPACE" == 'xsede' ]; then
    local ADDITIONAL_GROUP_COMMAND1="chmod $GROUPS_LOC/gffs-admins +rwx $userpath"
    local ADDITIONAL_GROUP_COMMAND2="onerror admin chmod for gffs-admins group membership failed."
    local ADDITIONAL_GROUP_COMMAND3="ln $GROUPS_LOC/gffs-admins $userpath/gffs-admins"
    local ADDITIONAL_GROUP_COMMAND4="onerror admin chmod for gffs-admins group membership failed."
    local ADDITIONAL_GROUP_COMMAND5="chmod $GROUPS_LOC/gffs-amie +rwx $userpath"
    local ADDITIONAL_GROUP_COMMAND6="onerror admin chmod for gffs-amie group membership failed."
    local ADDITIONAL_GROUP_COMMAND7="ln $GROUPS_LOC/gffs-amie $userpath/gffs-amie"
    local ADDITIONAL_GROUP_COMMAND8="onerror admin chmod for gffs-amie group membership failed."
  fi

  if [ ! -z "$queue_perms" ]; then
    local QUEUE_CMD1="chmod $QUEUES_LOC/* +rwx $userpath"
    local QUEUE_CMD2="onerror admin chmod for $QUEUES_LOC/* failed."
  fi

  if [ ! -z "$bes_perms" ]; then
    local BES_CMD1="chmod $BES_CONTAINERS_LOC/* +rwx $userpath"
    local BES_CMD2="onerror admin chmod for $BES_CONTAINERS_LOC/* failed."
  fi

  multi_grid <<eof
    echo working on $userpath
    chmod "$userpath" +rwx "$userpath"
    onerror admin chmod for $userpath failed.
    chmod "/" +rwx $userpath
    onerror admin chmod for / failed.

    echo working on container perms
    chmod "$CONTAINERS_LOC/*" +rwx $userpath
    onerror admin chmod for $CONTAINERS_LOC failed.
    chmod "$BOOTSTRAP_LOC/*" +rwx $userpath
    onerror admin chmod for $BOOTSTRAP_LOC/* failed.
    chmod "$BOOTSTRAP_LOC/Services/*" +rwx $userpath
    onerror admin chmod for $BOOTSTRAP_LOC/Services/* failed.

    echo working on queue holding area
    chmod "$QUEUES_LOC" +rwx $userpath
    onerror admin chmod for $QUEUES_LOC failed.

    echo performing optional queue commands
    $QUEUE_CMD1
    $QUEUE_CMD2

    echo working on bes holding area
    chmod "$BES_CONTAINERS_LOC" +rwx $userpath
    onerror admin chmod for $BES_CONTAINERS_LOC failed.

    echo performing optional bes commands
    $BES_CMD1
    $BES_CMD2

    echo fixing users and groups and homes
    chmod -R "$USERS_LOC" +rwx $userpath
    onerror admin chmod for $USERS_LOC failed.
    chmod -R "$GROUPS_LOC" +rwx $userpath
    onerror admin chmod for $GROUPS_LOC failed.
    chmod -R "$HOMES_LOC" +rwx $userpath
    onerror admin chmod for $HOMES_LOC/* failed.

    echo optional group membership steps
    $ADDITIONAL_GROUP_COMMAND1
    $ADDITIONAL_GROUP_COMMAND2
    $ADDITIONAL_GROUP_COMMAND3
    $ADDITIONAL_GROUP_COMMAND4
    $ADDITIONAL_GROUP_COMMAND5
    $ADDITIONAL_GROUP_COMMAND6
    $ADDITIONAL_GROUP_COMMAND7
    $ADDITIONAL_GROUP_COMMAND8

    echo end of admin privileges procedure
eof
  check_if_failed "Administrative steps failed for $userpath"
}

##############

