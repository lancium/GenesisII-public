#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

if [ $# -ne 4 ]
then
	echo "USAGE:  $0 <trust-store> <trust-store-password> <new-cert-file> <new-cert-alias>"
	exit 1
fi

TRUST_STORE="$1"
TRUSTED_PASS="$2"
CERT_FILE="$3"
OUTPUT_ALIAS="$4"
"$GENII_BINARY_DIR/cert-tool" import -output-keystore="$TRUST_STORE" -output-keystore-pass="$TRUSTED_PASS" -base64-cert-file="$CERT_FILE" -output-alias="$OUTPUT_ALIAS"
