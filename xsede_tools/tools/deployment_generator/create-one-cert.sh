#!/bin/bash

# creates a resource signing keypair for a container based on the root of
# RNS container's CA certificate.

####

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

# pull in the xsede test base support.
source $WORKDIR/../../prepare_tests.sh $WORKDIR/../../prepare_tests.sh

# if that didn't work, complain.
if [ -z "$XSEDE_TEST_ROOT" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
# otherwise load the rest of the tool environment.
source $XSEDE_TEST_ROOT/library/establish_environment.sh

####

cert_to_create="$1"; shift
cert_pass="$1"; shift
cert_alias="$1"; shift
if [ -z "$cert_to_create" -o -z "$cert_pass" -o -z "$cert_alias" ]; then
  echo "This script needs (1) the name of a PFX file to create, (2) a password to"
  echo "secure that file, and (3) the alias for the key-pair in the PFX.  The PFX"
  echo "file will be generated from the current CA certificate in generated-certs."
  exit 1
fi
if [ "$cert_alias" == "$CA_ALIAS" ]; then
  echo "It is crucial for the new signing certificate to not conflict with the root"
  echo "signing certificate's alias, but you have specified them as identical"
  echo "strings.  Please choose a different alias for the new certificate that is"
  echo "not equal to '$CA_ALIAS'."
  exit 1
fi

##############

# load the functions for working with certificates.
source certificate-methods.sh

setup_key_variables  # get names of certs etc.

##############

create_certificate_using_CA "$GENERATED_CERTS/$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "$cert_to_create" "$cert_pass" "$cert_alias" "GenesisII Cert $(basename "$cert_to_create" "pfx")"

