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
  if [ ! -f "$GENII_BINARY_DIR/cert-tool" ]; then
    echo "The GENII_INSTALL_DIR does not appear to have cert-tool available.  Is this"
    echo "the correct installation directory, or perhaps the code needs to be rebuilt?"
    exit 1
  fi
}

####
# borrowed from feisty meow code base with permission of chris koeritz.
####
  # sets the variable in parameter 1 to the value in parameter 2, but only if
  # that variable was undefined.
  function set_var_if_undefined()
  {
    local var_name="$1"; shift
    local var_value="$1"; shift
    if [ -z "${!var_name}" ]; then
      eval export $var_name="$var_value"
    fi
  }
####

# sets up the variables for the deployment generator.  this allows these to
# have been set beforehand in some cases.
function setup_key_depgen_variables()
{
  validate_configuration

  ##############

  # we don't allow the variables in this section to be overridden.

  # folder where the grid admin can add replacement certificates.
  export KEY_OVERRIDES=override_keys

  export DEPLOYMENT_MEMORY_FILE=saved-deployment-info.txt

  ##############

  # the variables in this section may be overridden by setting them before
  # this function is called.

  # definitions of the certificate file names, passwords and aliases.

  set_var_if_undefined TRUSTSTORE_PASSWORD trusted
#  export TRUSTSTORE_PASSWORD='trusted'
  set_var_if_undefined TRUSTSTORE_PFX trusted.pfx
#  export TRUSTSTORE_PFX=trusted.pfx

  set_var_if_undefined TLS_IDENTITY_PFX tls-cert.pfx
#  export TLS_IDENTITY_PFX=tls-cert.pfx
  set_var_if_undefined TLS_IDENTITY_PASSWORD container
#  if [ -z "$TLS_IDENTITY_PASSWORD" ]; then
#    export TLS_IDENTITY_PASSWORD='container'
#  fi

  set_var_if_undefined CA_ALIAS signing-cert
#  export CA_ALIAS='signing-cert'
  set_var_if_undefined CA_PFX signing-cert.pfx
#  export CA_PFX=signing-cert.pfx
  set_var_if_undefined CA_CERT_FILE signing-cert.cer
#  export CA_CERT_FILE=signing-cert.cer

  set_var_if_undefined ADMIN_PFX admin.pfx
#  export ADMIN_PFX=admin.pfx
  set_var_if_undefined ADMIN_CERT_FILE admin.cer
#  export ADMIN_CERT_FILE=admin.cer
}

##############

# takes the full DNS name of the host and the short deployment name.
function generate_all_certificates()
{
  local MACHINE_NAME="$1"; shift
  if [ -z "$MACHINE_NAME" ]; then
    echo This script needs the DNS name for your container as the first parameter.
    exit 1
  fi

  local DEPLOYMENT_NAME="$1"; shift
  if [ -z "$DEPLOYMENT_NAME" ]; then
    echo "The deployment name must be specified as the second parameter."
    exit 1
  fi
  if [ "$DEPLOYMENT_NAME" == default ]; then
    echo "The default deployment *must* *not* be modified with this script."
    echo "Please pick a different name for your new deployment."
    exit 1
  fi

  echo "This container will be the *root* of RNS: it is the BootstrapContainer."

  if [ ! -f certificate-config.txt ]; then
    echo "There is no 'certificate-config.txt' file.  Please copy the example version"
    echo "to that filename and edit it for your locale certificate information."
    return 1
  fi

  ##############

  # get names of certs etc.
#hmmm: we need to not call this if the vars were set by the bootstrapping process already.
# maybe just have the setup functions skip if already set?
  setup_key_depgen_variables

  ##############

  # Perform some house-keeping on generated files.

  rm -f "$DEPLOYMENT_MEMORY_FILE"

  ##############

  # the variable ACTUAL_DEPLOYMENT_FOLDER points at where we'll install the deployment.
  # this is established by our config consuming script.

  # copy our template into place for the deployment.
  if [ -d "$ACTUAL_DEPLOYMENT_FOLDER" ]; then
    echo "Destroying existing deployment folder: $ACTUAL_DEPLOYMENT_FOLDER"
    \rm -rf "$ACTUAL_DEPLOYMENT_FOLDER"
  fi

  # create the directory for storing into.
  cp -f -R $GENII_INSTALL_DIR/deployments/default "$ACTUAL_DEPLOYMENT_FOLDER"
  check_if_failed "copying template deployment into place for $DEPLOYMENT_NAME"

  cp -f "$GFFS_TOOLKIT_ROOT/tools/deployment_generator/support_files"/* "$ACTUAL_DEPLOYMENT_FOLDER"
  check_if_failed "copying deployment configuration file into place"

  # clean out things we don't pass along to the generated deployment, since the
  # real default deployment from the install should provide these.
  \rm -rf "$ACTUAL_DEPLOYMENT_FOLDER/services" "$ACTUAL_DEPLOYMENT_FOLDER/configuration/cservices" "$ACTUAL_DEPLOYMENT_FOLDER"/configuration/filesystems.xml "$ACTUAL_DEPLOYMENT_FOLDER"/configuration/global-bes-config.xml "$ACTUAL_DEPLOYMENT_FOLDER"/configuration/client-config.xml

  SECURITY_DIR="$ACTUAL_DEPLOYMENT_FOLDER/security"

  # load in the locally defined certificates.

  rsync -r trusted-certificates "$SECURITY_DIR"
  check_if_failed "copying trusted-certificates into $ACTUAL_DEPLOYMENT_FOLDER"

  rsync -r grid-certificates "$SECURITY_DIR"
  check_if_failed "copying trusted-certificates into $ACTUAL_DEPLOYMENT_FOLDER"

  rsync -r myproxy-certs "$SECURITY_DIR"
  check_if_failed "copying myproxy-certs into $ACTUAL_DEPLOYMENT_FOLDER"

  # copy a pre-defined TLS keypair if it exists.
  if [ -f "$KEY_OVERRIDES/$TLS_IDENTITY_PFX" ]; then
    cp "$KEY_OVERRIDES/$TLS_IDENTITY_PFX" "$ACTUAL_DEPLOYMENT_FOLDER/security"
  fi

  # copy a pre-defined admin keypair if it exists.
  if [ -f "$KEY_OVERRIDES/$ADMIN_PFX" ]; then
    cp "$KEY_OVERRIDES/$ADMIN_PFX" "$ACTUAL_DEPLOYMENT_FOLDER/security"
  fi

  if [ -z "$FOLDERSPACE" ]; then
    echo "Defaulting to xsede.org for FOLDERSPACE variable."
    FOLDERSPACE=xsede.org
  fi

  echo Copying xsede namespace properties into place.
  cp $ACTUAL_DEPLOYMENT_FOLDER/configuration/template-namespace.properties $NAMESPACE_FILE
  check_if_failed "copying template namespace properties file to real one"

  ##############
  
  # we regenerate the certificates as needed.

  # generate the certificate authority signing key PFX.
  local UBER_CA_PFX="$CA_PFX-base.pfx"
  local UBER_CA_ALIAS="base-key"

  local DN_STRING="$(accumulate_DN "certificate-config.txt" "GenesisII Certificate Base")"
  echo "generating cert with DN as: $DN_STRING"
  "$GENII_BINARY_DIR/cert-tool" gen "-dn=$DN_STRING" -output-storetype=PKCS12 "-output-entry-pass=$CA_PASSWORD" -output-keystore=$SECURITY_DIR/$UBER_CA_PFX "-output-keystore-pass=$CA_PASSWORD" "-output-alias=$UBER_CA_ALIAS" -keysize=2048
  check_if_failed "generating base certificate PFX"

  create_pfx_using_CA "$SECURITY_DIR/$UBER_CA_PFX" "$CA_PASSWORD" "$UBER_CA_ALIAS" "$SECURITY_DIR/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "GenesisII Certificate Authority"
  check_if_failed "generating CA PFX"

  if [ ! -f "$ACTUAL_DEPLOYMENT_FOLDER/configuration/security.properties" ]; then
    echo "The security.properties file is missing.  It seems the deployment directory"
    echo "provided is not complete: '$ACTUAL_DEPLOYMENT_FOLDER'"
    exit 1
  fi
  
#old descrip
  # this is a bootstrap, rns root, container.  we will create TRUSTSTORE_PFX.
  # we only include the trusted certificates from the overrides folder,
  # as well as the newly created CA cert for signing resources on the root.
  for CERT_FILE in $SECURITY_DIR/$CA_CERT_FILE ; do
    if [ ! -f $CERT_FILE ]; then continue; fi
    echo -e "\nAdding certificate '$CERT_FILE' to trust store.\n"
    OUTPUT_ALIAS=$(basename "$CERT_FILE" .cer)
    echo $OUTPUT_ALIAS
    "$GENII_BINARY_DIR/cert-tool" import -output-keystore="$SECURITY_DIR/$TRUSTSTORE_PFX" -output-keystore-pass="$TRUSTSTORE_PASSWORD" -base64-cert-file="$CERT_FILE" -output-alias="$OUTPUT_ALIAS"
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
}

# this function is heavily dependent on the generate_all_certificates function
# having been previously run.  it requires many of the variables set up by
# that function.  given those prerequisites, it will fill out the rest of the
# deployment folder with the generated certificates and appropriate configuration
# files.
function populate_deployment()
{
  if [ ! -d "$ACTUAL_DEPLOYMENT_FOLDER" ]; then
    echo The deployment directory specified does not exist: $ACTUAL_DEPLOYMENT_FOLDER
    exit 1
  fi

  ###############

  if [ ! -d "$ACTUAL_DEPLOYMENT_FOLDER/security/default-owners" ]; then
    mkdir -p "$ACTUAL_DEPLOYMENT_FOLDER/security/default-owners"
    check_if_failed "creating default-owners directory"
  fi

  # make sure we put the admin certificate in as the default owner.
  cp $SECURITY_DIR/$ADMIN_CERT_FILE "$SECURITY_DIR/default-owners/$ADMIN_CERT_FILE"
  check_if_failed "copying administrative certificate to owners"

  if [[ $EXTRA_DEPGEN_FLAGS =~ .*ADDOWNER.* ]]; then
    echo Performing extra step to make the admin cert become the owner cert of the container.
    add_in_bootstrapped_certs
  fi

#  # fix the container's web port.
#  replace_phrase_in_file "$ACTUAL_DEPLOYMENT_FOLDER/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.listen-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$PORT"
#  check_if_failed "fixing web container main port"
#  replace_phrase_in_file "$ACTUAL_DEPLOYMENT_FOLDER/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.dpages-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$(expr $PORT + 1)"
#  check_if_failed "fixing web container dpages port"

  # old: clean out any svn folders from the checkout.
  find "$ACTUAL_DEPLOYMENT_FOLDER" -depth -type d -iname "\.svn" -exec rm -rf {} ';'

  # get the container properties right.
  cp "$GENII_INSTALL_DIR/lib/container.properties.template" "$GENII_INSTALL_DIR/lib/container.properties"
  check_if_failed "copying container properties template"

  # set up a client properties file.
  cp "$GENII_INSTALL_DIR/lib/client.properties.template" "$GENII_INSTALL_DIR/lib/client.properties"
  check_if_failed "copying client properties template"

#  replace_phrase_in_file "$GENII_INSTALL_DIR/lib/client.properties" "^.*edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$DEPLOYMENT_NAME"
#  check_if_failed "fixing client properties for deployment"

  # remove any old context.xml, since we are making a new one now.
  rm -f context.xml
  echo Preparing bootstrap script for GFFS root.
  # the bootstrap_grid method sets up a bootstrap script and does all the
  # configuration file editing that we need.
  bootstrap_grid
  check_if_failed "bootstrapping test grid"

#  BOOTSTRAP_FILE="$ACTUAL_DEPLOYMENT_FOLDER/configuration/bootstrap.xml"
#  cp "$ACTUAL_DEPLOYMENT_FOLDER/configuration/template-bootstrap.xml" "$BOOTSTRAP_FILE"

#  echo -e "\
#DEPLOYMENT_NAME=$DEPLOYMENT_NAME\n\
#" >$DEPLOYMENT_MEMORY_FILE
#  check_if_failed "writing deployment information for packaging scripts"

  echo -e "\n\nCertificates generated for Root of RNS / Bootstrap container on this host\nand the deployment $DEPLOYMENT_NAME has been populated."
}

