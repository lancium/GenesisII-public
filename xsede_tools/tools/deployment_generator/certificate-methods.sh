#!/bin/bash

# functions that are helpful for creating the key-pairs needed for a GenesisII container.
#
# Author: Chris Koeritz

##############

function validate_configuration()
{
  DEP_TYPE="$1"; shift
  if [ "$DEP_TYPE" != client ]; then
    if [ ! -f passwords.txt ]; then
      echo The passwords.txt file needs to be created.  There is an example
      echo version to show the format.
      exit 1
    fi

    source passwords.txt

    if [ -z "$CA_PASSWORD" -o -z "$CA_ALIAS" -o -z "$ADMINPASS" -o -z "$TLSIDENTITYPASS" ]; then
      echo One of the passwords was not defined in the passwords.txt file.
      echo The following are required: CA_PASSWORD, CA_ALIAS, ADMINPASS, TLSIDENTITYPASS.
      exit 1
    fi
  else
    # we make up some passwords so they're not blank when we do replacements.
    CA_PASSWORD=nop
    CA_ALIAS=nop
    ADMINPASS=nop
    TLSIDENTITYPASS=nop
  fi

  ##############

  # make sure we know where to find the install directory.

  if [ -z "$GENII_INSTALL_DIR" -o ! -d "$GENII_INSTALL_DIR" ]; then
    echo The GENII_INSTALL_DIR variable must be set to the location of your GenesisII
    echo installation before running this script.
    exit 1
  fi
  if [ -z "$JAVA_HOME" -o ! -d "$JAVA_HOME" -o ! -d "$JAVA_HOME/bin" ]; then
    echo The JAVA_HOME variable must be set to point at Java for this computer.
    echo The standard is for there to be a bin folder under that location.
    exit 1
  fi
  if [ ! -f "$GENII_INSTALL_DIR/cert-tool" ]; then
    echo "The GENII_INSTALL_DIR does not appear to have cert-tool available.  Is this"
    echo "the correct installation directory, or perhaps the code needs to be rebuilt?"
    exit 1
  fi
}

function setup_key_variables()
{
  validate_configuration "$1"

  # These should *not* be changed.
  TRUSTEDPASS='trusted'
  if [ -z "$TLSIDENTITYPASS" ]; then
    TLSIDENTITYPASS='container'
  fi

  ##############

  ADMIN_ALIAS="admin"

  # definitions of the certificate file names.

  # where we put all final versions of certificates.
  GENERATED_CERTS=generated_certs
  # where the grid admin can add trusted certs.
  TRUSTED_CERTS=trusted_certs
  # where we'll put the resources that anything in the grid needs.
  GRIDWIDE_CERTS=gridwide_certs

  TRUSTEDPFX=trusted.pfx

  CA_PFX=signing-cert.pfx
  CACER=signing-cert.cer
  TLSIDENTITYPFX=tls-cert.pfx
  ADMINPFX=admin.pfx
  # admincer is still used for populating default owners.
  ADMINCER=admin.cer

  DEP_INFO_FILE=saved_deployment_info.txt
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

  DEP_TYPE="$1"; shift
  if [ "$DEP_TYPE" == client ]; then
    echo "This is a client deployment."
  elif [ "$DEP_TYPE" != bootstrap ]; then
    echo "This container will *NOT* be the root of RNS."
  else
    echo "This container will be the *root* of RNS: it is the BootstrapContainer."
  fi

  # optional parameter says don't regenerate stuff, just use what we did before.
  REDEPLOY="$1"; shift

  if [ ! -f certificate-config.txt ]; then
    echo "There is no 'certificate-config.txt' file.  Please copy the example version"
    echo "to that filename and edit it for your locale certificate information."
    return 1
  fi
  source certificate-config.txt

  ##############

  # get names of certs etc.
  setup_key_variables "$DEP_TYPE"

  ##############

  if [ -z "$REDEPLOY" ]; then
    # save our parameters in case we need to redeploy.
    echo -e "\n\
DEP_TYPE=$DEP_TYPE\n\
DEP_NAME=$DEP_NAME\n\
MACHINE_NAME=$MACHINE_NAME\n\
PORT=$PORT\n\
" >$DEP_INFO_FILE
  fi

  ##############

  # fail fast if they are giving us an inconsistent configuration.
  
  if [ "$DEP_TYPE" != bootstrap ]; then
    if [ ! -f "$GRIDWIDE_CERTS/$TRUSTEDPFX" ]; then
      echo "This is an inconsistent container configuration.  This is not the root of"
      echo "RNS (i.e., it's not a bootstrap container), but there is no $TRUSTEDPFX"
      echo "file in the $GRIDWIDE_CERTS folder."
      exit 1
    fi
  fi

  if [ -z "$REDEPLOY" ]; then
    # we don't re-copy the files for a redeployment operation.

    if [ -d "$GENERATED_CERTS" ]; then
      echo "Please remove the existing directory: $GENERATED_CERTS"
      echo "However, if you generated a grid's certificates into that directory, are"
      echo "you sure you want to regenerate them?  This may change some certificates."
      exit 1
    fi
    # clean up files we always will regenerate.
    mkdir "$GENERATED_CERTS"
  
    if [ "$DEP_TYPE" != bootstrap ]; then
      # this is not the bootstrap.

      # copy in any trusted certificates they want us to re-use.
      for i in "$TRUSTED_CERTS"/*; do
	if [ -f "$i" -o -d "$i" ]; then
	  cp -R "$i" "$GENERATED_CERTS"
	  check_if_failed "copying trusted certificate '$i' into generated directory"
	fi
      done
      # then copy in the grid-wide certs, which override other items.
      for i in "$GRIDWIDE_CERTS"/*; do
	if [ -f "$i" -o -d "$i" ]; then
	  cp -R "$i" "$GENERATED_CERTS"
	  check_if_failed "copying grid-wide certificate '$i' into generated directory"
	fi
      done
    else
      # this is the bootstrap container.

      # clean up any grid-wide stuff, since we're making that now.
      \rm -rf "$GRIDWIDE_CERTS"
      mkdir "$GRIDWIDE_CERTS"
      check_if_failed "making merged certificates directory"
      # copy in any trusted certificates they want us to re-use.
      for i in "$TRUSTED_CERTS"/*; do
	if [ -f "$i" -o -d "$i" ]; then
	  cp -R "$i" "$GRIDWIDE_CERTS"
	  check_if_failed "copying trusted certificate '$i' into grid-wide directory"
	fi
      done
      # then copy in the grid-wide certs, which override other items.
      for i in "$GRIDWIDE_CERTS"/*; do
	if [ -f "$i" -o -d "$i" ]; then
	  cp -R "$i" "$GENERATED_CERTS"
	  check_if_failed "copying grid-wide certificate '$i' into generated directory"
	fi
      done
    fi
  fi
  
  ##############
  
  # where we'll install the deployment.
  DEP_DIR="$GENII_INSTALL_DIR/deployments/$DEP_NAME"
  
  # copy our template into place for the deployment.
  if [ -d "$DEP_DIR" ]; then
    echo "Destroying existing deployment folder: $DEP_DIR"
    \rm -rf "$DEP_DIR"
  fi
  cp -f -R deployment-template "$DEP_DIR"
  check_if_failed "copying template deployment into place for $DEP_NAME"
  
  if [ $NAMESPACE == 'xsede' ]; then
    echo Copying xsede namespace properties into place.
    cp $DEP_DIR/configuration/xsede-namespace.properties $DEP_DIR/configuration/namespace.properties
  elif [ $NAMESPACE == 'xcg' ]; then
    echo Copying xcg namespace properties into place.
    cp $DEP_DIR/configuration/xcg-namespace.properties $DEP_DIR/configuration/namespace.properties
  else
    echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
    exit 1
  fi

  ##############
  
  if [ "$DEP_TYPE" != client ]; then
    # we only regenerate these certificates (when they're missing) if this is not a client.
    if [ ! -f "$GENERATED_CERTS/$CA_PFX" ]; then
      # generate the certificate authority signing key PFX.
      local UBER_CA_PFX="$CA_PFX-base.pfx"
      local UBER_CA_ALIAS="base-key"

      local DN_STRING="$(calculate_DN "GenesisII Certificate Base")"
      echo "generating cert with DN as: $DN_STRING"
      $GENII_INSTALL_DIR/cert-tool gen "-dn=$DN_STRING" -output-storetype=PKCS12 "-output-entry-pass=$CA_PASSWORD" -output-keystore=$GENERATED_CERTS/$UBER_CA_PFX "-output-keystore-pass=$CA_PASSWORD" "-output-alias=$UBER_CA_ALIAS" -keysize=2048
      check_if_failed "generating base certificate PFX"

      create_certificate_using_CA "$GENERATED_CERTS/$UBER_CA_PFX" "$CA_PASSWORD" "$UBER_CA_ALIAS" "$GENERATED_CERTS/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "GenesisII Certificate Authority"
      check_if_failed "generating CA PFX"
    fi

    if [ ! -f "$DEP_DIR/configuration/security.properties" ]; then
      echo "The security.properties file is missing.  It seems the deployment directory"
      echo "provided is not accurate: '$DEP_DIR'"
      exit 1
    fi
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
  
  if [ "$DEP_TYPE" == bootstrap ]; then
    if [ -z "$REDEPLOY" ]; then
      # this is a bootstrap, rns root, container.  we will create TRUSTEDPFX.
      # we only include the trusted certificates from trusted_certs, as well as the
      # newly created CA cert for signing resources on the root, and the secure runnable
      # certificate for patching(?).
      for CERT_FILE in $GENERATED_CERTS/*.cer; do
        echo -e "\nAdding certificate '$CERT_FILE' to trust store.\n"
        OUTPUT_ALIAS=$(basename "$CERT_FILE" .cer)
        echo $OUTPUT_ALIAS
        "$GENII_INSTALL_DIR/cert-tool" import -output-keystore="$GENERATED_CERTS/$TRUSTEDPFX" -output-keystore-pass="$TRUSTEDPASS" -base64-cert-file="$CERT_FILE" -output-alias="$OUTPUT_ALIAS"
        check_if_failed "running cert tool on $CERT_FILE"
      done
      # make sure we move the newly built (with our certs plus any existing ones).
      cp "$GENERATED_CERTS/trusted.pfx" "$GRIDWIDE_CERTS"
    fi
  fi
  
  if [ "$DEP_TYPE" != client ]; then
    # the admin cert does not go into the trust store.
    if [ "$DEP_TYPE" == bootstrap ]; then
      # bootstrap always gets a new admin.pfx.
      if [ ! -f "$GENERATED_CERTS/$ADMINPFX" ]; then
        # generate the administrator key.
        create_certificate_using_CA "$GENERATED_CERTS/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "$GENERATED_CERTS/$ADMINPFX" "$ADMINPASS" "$ADMIN_ALIAS" "Administrator Certificate"
        check_if_failed "creating $ADMINPFX from $CA_PFX"
        cp "$GENERATED_CERTS/$ADMINCER" "$GRIDWIDE_CERTS"
        check_if_failed "copying $ADMINCER into $GRIDWIDE_CERTS"
      fi
    else
      # just copy the main admin certificate into the generated certs.
      cp "$GRIDWIDE_CERTS/$ADMINCER" "$GENERATED_CERTS"
      check_if_failed "copying $ADMINCER from $GRIDWIDE_CERTS into $GENERATED_CERTS"
    fi

    if [ ! -f "$GENERATED_CERTS/$TLSIDENTITYPFX" ]; then
      # make new container cert from net root cert (for bootstrap container)
      echo "Making container cert for $MACHINE_NAME and placing in store $GENERATED_CERTS/$TLSIDENTITYPFX"
      create_certificate_using_CA "$GENERATED_CERTS/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "$GENERATED_CERTS/$TLSIDENTITYPFX" "$TLSIDENTITYPASS" "container" "$MACHINE_NAME"
      check_if_failed "creating $TLSIDENTITYPFX from $CA_PFX"
    fi
  fi
  
  # fix up the security properties with our container's tls key.
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-store=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store=$TLSIDENTITYPFX"
  check_if_failed "fixing key-store name for container"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-password=$TLSIDENTITYPASS"
  check_if_failed "fixing key password for container"
  replace_phrase_in_file "$DEP_DIR/configuration/security.properties" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=.*" "edu.virginia.vcgr.genii.container.security.ssl.key-store-password=$TLSIDENTITYPASS"
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

  if [ "$DEP_TYPE" != client ]; then
    # make sure we put the admin certificate in as the default owner.
    cp $GENERATED_CERTS/$ADMINCER "$DEP_DIR/security/default-owners/$ADMINCER"
    check_if_failed "copying administrative certificate to owners"
  fi

  # copy all the other certificates we created into the deployment.
  cp -f -R "$GENERATED_CERTS"/* "$DEP_DIR/security"
  check_if_failed "copying generated certificates to deployment's security folder"

  # fix the container's web port.
  replace_phrase_in_file "$DEP_DIR/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.listen-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$PORT"
  check_if_failed "fixing web container main port"
  replace_phrase_in_file "$DEP_DIR/configuration/web-container.properties" "edu.virginia.vcgr.genii.container.dpages-port=.*" "edu.virginia.vcgr.genii.container.listen-port=$(expr $PORT + 1)"
  check_if_failed "fixing web container dpages port"

  # clean out any svn folders from the checkout.
  find "$DEP_DIR" -depth -type d -iname "\.svn" -exec rm -rf {} ';'

  # get the container properties right.
  cp "$GENII_INSTALL_DIR/container.properties.example" "$GENII_INSTALL_DIR/container.properties"
  check_if_failed "copying container properties example"
  replace_phrase_in_file "$GENII_INSTALL_DIR/container.properties" "edu.virginia.vcgr.genii.container.deployment-name=.*" "edu.virginia.vcgr.genii.container.deployment-name=$DEP_NAME"
  check_if_failed "fixing container properties for deployment"

  if [ ! -z "$IS_BOOTSTRAP_CONTAINER" ]; then
    # fix the bootstrap script to have all the right values.
    echo Preparing bootstrap script for GFFS root.
    bootstrap_file="$DEP_DIR/configuration/bootstrap.xml"
    if [ $NAMESPACE == 'xsede' ]; then
      cp "$DEP_DIR/configuration/xsede-bootstrap.xml" "$bootstrap_file"
    elif [ $NAMESPACE == 'xcg' ]; then
      cp "$DEP_DIR/configuration/xcg-bootstrap.xml" "$bootstrap_file"
    else
      echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
      exit 1
    fi
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
    replace_phrase_in_file "$bootstrap_file" "REPLACEADMINPASSWORD" "$ADMINPASS"
    check_if_failed "fixing bootstrap for admin password"
    replace_phrase_in_file "$bootstrap_file" "REPLACEADMINPFX" "$ADMINPFX"
    check_if_failed "fixing bootstrap for admin pfx"
    replace_phrase_in_file "$bootstrap_file" "REPLACEADMINALIAS" "$ADMIN_ALIAS"
    check_if_failed "fixing bootstrap for admin alias"
  fi

  did_what="Certificates generated"
  container_type="Root of RNS / Bootstrap container"
  if [ -z "$IS_BOOTSTRAP_CONTAINER" ]; then
    container_type="non-bootstrap, ordinary container"
    if [ "$DEP_TYPE" == client ]; then
      container_type="client"
      did_what="Configuration created"
    fi
  fi
  if [ ! -z "$REDEPLOY" ]; then
    did_what="Re-used configuration"
  fi
  echo -e "\n\n$did_what for $container_type on this host\nand the deployment $DEP_NAME has been populated."
}

