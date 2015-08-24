#!/bin/bash

# functions that are helpful for creating the key-pairs needed for a GenesisII container.
#
# Author: Chris Koeritz

##############

function validate_configuration()
{
  if [ ! -f passwords.txt ]; then
    echo The passwords.txt file needs to be created.  There is an example
    echo version to show the format.
    exit 1
  fi

  source passwords.txt

  if [ -z "$CA_PASSWORD" -o -z "$ADMIN_PASSWORD" -o -z "$TLS_IDENTITY_PASSWORD" ]; then
    echo One of the passwords was not defined in the passwords.txt file.
    echo The following are required: CA_PASSWORD, ADMIN_PASSWORD, TLS_IDENTITY_PASSWORD.
    exit 1
  fi

  ##############

  # make sure we know where to find the install directory.

  if [ -z "$GENII_INSTALL_DIR" -o ! -d "$GENII_INSTALL_DIR" ]; then
    echo The GENII_INSTALL_DIR variable must be set to the location of your GenesisII
    echo installation before running this script.
    exit 1
  fi
#below should be unnecessary with new JAVA_HOME guessing code in prepare_tools.
#  if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" -o ! -d "$JAVA_HOME/bin" ]; then
#    echo The JAVA_HOME variable must be set to point at Java for this computer.
#    echo The standard is for there to be a bin folder under that location.
#    exit 1
#  fi
  if [ ! -f "$GENII_INSTALL_DIR/cert-tool" ]; then
    echo "The GENII_INSTALL_DIR does not appear to have cert-tool available.  Is this"
    echo "the correct installation directory, or perhaps the code needs to be rebuilt?"
    exit 1
  fi
}

function setup_key_variables()
{
  validate_configuration "$1"

  ##############

  # folder where the grid admin can add replacement certificates.
  KEY_OVERRIDES=override_keys

  ##############

  # definitions of the certificate file names, passwords and aliases.

  TRUSTSTORE_PASSWORD='trusted'
  TRUSTSTORE_PFX=trusted.pfx

  TLS_IDENTITY_PFX=tls-cert.pfx
  if [ -z "$TLS_IDENTITY_PASSWORD" ]; then
    TLS_IDENTITY_PASSWORD='container'
  fi

  CA_ALIAS='signing-cert'
  CA_PFX=signing-cert.pfx
  CA_CERT_FILE=signing-cert.cer

  ADMIN_PFX=admin.pfx
  ADMIN_CERT_FILE=admin.cer

  ##############

  DEPLOYMENT_MEMORY_FILE=saved-deployment-info.txt
}

##############

# takes the full DNS name of the host and the short deployment name.
function generate_all_certificates()
{
  MACHINE_NAME="$1"; shift
  if [ -z "$MACHINE_NAME" ]; then
    echo This script needs the DNS name for your container as the first parameter.
    exit 1
  fi

  DEP_NAME="$1"; shift
  if [ -z "$DEP_NAME" ]; then
    echo "The deployment name must be specified as the second parameter."
    exit 1
  fi
  if [ "$DEP_NAME" == default ]; then
    echo "The default deployment *must*not* be redone with this script."
    echo "Please pick a different name for your new deployment."
    exit 1
  fi

  echo "This container will be the *root* of RNS: it is the BootstrapContainer."

  if [ ! -f certificate-config.txt ]; then
    echo "There is no 'certificate-config.txt' file.  Please copy the example version"
    echo "to that filename and edit it for your locale certificate information."
    return 1
  fi
  #source certificate-config.txt

  ##############

  # get names of certs etc.
  setup_key_variables

  ##############

  # Perform some house-keeping on generated files.

  rm -f "$DEPLOYMENT_MEMORY_FILE"

  ##############

  # where we'll install the deployment.
  DEP_DIR="$GENII_INSTALL_DIR/deployments/$DEP_NAME"
  
  # copy our template into place for the deployment.
  if [ -d "$DEP_DIR" ]; then
    echo "Destroying existing deployment folder: $DEP_DIR"
    \rm -rf "$DEP_DIR"
  fi

  # create the directory for storing into.
  cp -f -R deployment-template "$DEP_DIR"
  check_if_failed "copying template deployment into place for $DEP_NAME"

  SECURITY_DIR="$DEP_DIR/security"

  # load in the locally defined certificates.

  rsync -r trusted-certificates "$SECURITY_DIR"
  check_if_failed "copying trusted-certificates into $DEP_DIR"

  rsync -r grid-certificates "$SECURITY_DIR"
  check_if_failed "copying trusted-certificates into $DEP_DIR"

  rsync -r myproxy-certs "$SECURITY_DIR"
  check_if_failed "copying myproxy-certs into $DEP_DIR"

  # copy a pre-defined TLS keypair if it exists.
  if [ -f "$KEY_OVERRIDES/$TLS_IDENTITY_PFX" ]; then
    cp "$KEY_OVERRIDES/$TLS_IDENTITY_PFX" "$DEP_DIR/security"
  fi

  # copy a pre-defined admin keypair if it exists.
  if [ -f "$KEY_OVERRIDES/$ADMIN_PFX" ]; then
    cp "$KEY_OVERRIDES/$ADMIN_PFX" "$DEP_DIR/security"
  fi

  if [ -z "$FOLDERSPACE" ]; then
    echo "Defaulting to xsede.org for FOLDERSPACE variable."
    FOLDERSPACE=xsede.org
  fi

  namespace_file=$DEP_DIR/configuration/namespace.properties
  echo Copying xsede namespace properties into place.
  cp $DEP_DIR/configuration/template-namespace.properties $namespace_file

  replace_phrase_in_file "$namespace_file" "FOLDERSPACE" "$FOLDERSPACE"
  check_if_failed "fixing namespace properties for folderspace variable"

  ##############
  
  # we regenerate the certificates as needed.

  # generate the certificate authority signing key PFX.
  local UBER_CA_PFX="$CA_PFX-base.pfx"
  local UBER_CA_ALIAS="base-key"

  local DN_STRING="$(accumulate_DN "certificate-config.txt" "GenesisII Certificate Base")"
  echo "generating cert with DN as: $DN_STRING"
  "$GENII_INSTALL_DIR/cert-tool" gen "-dn=$DN_STRING" -output-storetype=PKCS12 "-output-entry-pass=$CA_PASSWORD" -output-keystore=$SECURITY_DIR/$UBER_CA_PFX "-output-keystore-pass=$CA_PASSWORD" "-output-alias=$UBER_CA_ALIAS" -keysize=2048
  check_if_failed "generating base certificate PFX"

  create_pfx_using_CA "$SECURITY_DIR/$UBER_CA_PFX" "$CA_PASSWORD" "$UBER_CA_ALIAS" "$SECURITY_DIR/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "GenesisII Certificate Authority"
  check_if_failed "generating CA PFX"

  if [ ! -f "$DEP_DIR/configuration/security.properties" ]; then
    echo "The security.properties file is missing.  It seems the deployment directory"
    echo "provided is not complete: '$DEP_DIR'"
    exit 1
  fi
  
  # fix the configuration file with our CA information.
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.resource-identity.key-password=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-password=$CA_PASSWORD"
  check_if_failed "fixing key password for CA"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password=$CA_PASSWORD"
  check_if_failed "fixing store password for CA"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.resource-identity.container-alias=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.container-alias=$CA_ALIAS"
  check_if_failed "fixing alias for CA"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store=.*" "edu.virginia.vcgr.genii.container.security.resource-identity.key-store=$CA_PFX"
  check_if_failed "fixing key-store name for CA"
  
#old descrip
  # this is a bootstrap, rns root, container.  we will create TRUSTSTORE_PFX.
  # we only include the trusted certificates from the overrides folder,
  # as well as the newly created CA cert for signing resources on the root.
  for CERT_FILE in $SECURITY_DIR/$CA_CERT_FILE ; do
    if [ ! -f $CERT_FILE ]; then continue; fi
    echo -e "\nAdding certificate '$CERT_FILE' to trust store.\n"
    OUTPUT_ALIAS=$(basename "$CERT_FILE" .cer)
    echo $OUTPUT_ALIAS
    "$GENII_INSTALL_DIR/cert-tool" import -output-keystore="$SECURITY_DIR/$TRUSTSTORE_PFX" -output-keystore-pass="$TRUSTSTORE_PASSWORD" -base64-cert-file="$CERT_FILE" -output-alias="$OUTPUT_ALIAS"
    check_if_failed "running cert tool on $CERT_FILE"
  done

  # bootstrap gets a new admin.pfx unless it's provided in overrides.
  if [ ! -f "$SECURITY_DIR/$ADMIN_PFX" ]; then
    # generate the administrator key.
    create_pfx_using_CA "$SECURITY_DIR/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "$SECURITY_DIR/$ADMIN_PFX" "$ADMIN_PASSWORD" "$ADMIN_ALIAS" "Administrator Certificate"
    check_if_failed "creating $ADMIN_PFX from $CA_PFX"
  else
    export_certificate_from_pfx "$SECURITY_DIR/$ADMIN_CERT_FILE" "$SECURITY_DIR/$ADMIN_PFX" "$ADMIN_PASSWORD" "$ADMIN_ALIAS" 
  fi

  # bootstrap gets a generated TLS certificate unless present in overrides.
  if [ ! -f "$SECURITY_DIR/$TLS_IDENTITY_PFX" ]; then
    # make new container cert from net root cert (for bootstrap container)
    echo "Making container cert for $MACHINE_NAME and placing in store $SECURITY_DIR/$TLS_IDENTITY_PFX"
    create_pfx_using_CA "$SECURITY_DIR/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "$SECURITY_DIR/$TLS_IDENTITY_PFX" "$TLS_IDENTITY_PASSWORD" "container" "$MACHINE_NAME"
    check_if_failed "creating $TLS_IDENTITY_PFX from $CA_PFX"
  fi
  
  # fix up the security properties with our container's tls key.
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-store=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store=$TLS_IDENTITY_PFX"
  check_if_failed "fixing key-store name for container"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-password=$TLS_IDENTITY_PASSWORD"
  check_if_failed "fixing key password for container"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=$TLS_IDENTITY_PASSWORD"
  check_if_failed "fixing store password for container"
  
  # fix up the server config with our hostname.
  replace_phrase_in_file "$DEP_DIR/configuration/server-config.xml" "\(name=.edu.virginia.vcgr.genii.container.external-hostname-override. value=\"\)[^\"]*\"" "\1$MACHINE_NAME\""
}

# this function is heavily dependent on the generate_all_certificates function
# having been previously run.  it requires many of the variables set up by
# that function.  given those prerequisites, it will fill out the rest of the
# deployment folder with the generated certificates and appropriate configuration
# files.
function populate_deployment()
{
  if [ ! -d "$DEP_DIR" ]; then
    echo The deployment directory specified does not exist: $DEP_DIR
    exit 1
  fi

  ###############

  if [ ! -d "$DEP_DIR/security/default-owners" ]; then
    mkdir -p "$DEP_DIR/security/default-owners"
    check_if_failed "creating default-owners directory"
  fi

  # make sure we put the admin certificate in as the default owner.
  cp $SECURITY_DIR/$ADMIN_CERT_FILE "$SECURITY_DIR/default-owners/$ADMIN_CERT_FILE"
  check_if_failed "copying administrative certificate to owners"

  # fix the container's web port.
  replace_phrase_in_file "$DEP_DIR/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.listen-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$PORT"
  check_if_failed "fixing web container main port"
  replace_phrase_in_file "$DEP_DIR/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.dpages-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$(expr $PORT + 1)"
  check_if_failed "fixing web container dpages port"

  # old: clean out any svn folders from the checkout.
  find "$DEP_DIR" -depth -type d -iname "\.svn" -exec rm -rf {} ';'

  # get the container properties right.
  cp "$GENII_INSTALL_DIR/lib/container.properties.template" "$GENII_INSTALL_DIR/lib/container.properties"
  check_if_failed "copying container properties template"

  # set up a client properties file.
  cp "$GENII_INSTALL_DIR/lib/client.properties.template" "$GENII_INSTALL_DIR/lib/client.properties"
  check_if_failed "copying client properties template"
  replace_phrase_in_file "$GENII_INSTALL_DIR/lib/client.properties" "^.*edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$DEP_NAME"
  check_if_failed "fixing client properties for deployment"

  # remove any old context.xml, since we are making a new one now.
  rm -f context.xml
  # fix the bootstrap script to have all the right values.
  echo Preparing bootstrap script for GFFS root.
  bootstrap_file="$DEP_DIR/configuration/bootstrap.xml"
  cp "$DEP_DIR/configuration/template-bootstrap.xml" "$bootstrap_file"

  replace_phrase_in_file "$bootstrap_file" "FOLDERSPACE" "$FOLDERSPACE"
  check_if_failed "fixing bootstrap for folderspace variable"
  replace_phrase_in_file "$bootstrap_file" "REPLACEDEPNAME" "$DEP_NAME"
  check_if_failed "fixing bootstrap for deployment name"
  replace_phrase_in_file "$bootstrap_file" "REPLACEPORT" "$PORT"
  check_if_failed "fixing bootstrap for port"
  replace_phrase_in_file "$bootstrap_file" "REPLACECAPFX" "$CA_PFX"
  check_if_failed "fixing bootstrap for CA pfx"
  replace_phrase_in_file "$bootstrap_file" "REPLACECAPASSWORD" "$CA_PASSWORD"
  check_if_failed "fixing bootstrap for CA password"
  replace_phrase_in_file "$bootstrap_file" "REPLACECAALIAS" "$CA_ALIAS"
  check_if_failed "fixing bootstrap for CA alias"
  replace_phrase_in_file "$bootstrap_file" "REPLACEADMINPASSWORD" "$ADMIN_PASSWORD"
  check_if_failed "fixing bootstrap for admin password"
  replace_phrase_in_file "$bootstrap_file" "REPLACEADMINPFX" "$ADMIN_PFX"
  check_if_failed "fixing bootstrap for admin pfx"
  replace_phrase_in_file "$bootstrap_file" "REPLACEADMINALIAS" "$ADMIN_ALIAS"
  check_if_failed "fixing bootstrap for admin alias"

  echo -e "\
DEP_NAME=$DEP_NAME\n\
" >$DEPLOYMENT_MEMORY_FILE
  check_if_failed "writing deployment information for packaging scripts"

  echo -e "\n\nCertificates generated for Root of RNS / Bootstrap container on this host\nand the deployment $DEP_NAME has been populated."

}

