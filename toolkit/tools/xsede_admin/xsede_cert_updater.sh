#!/bin/bash

# where we'll install certificates into.
CERTSDIR="/etc/grid-security/certificates"

function print_instructions()
{
  scriptname="$(basename "$0")"
  echo "$scriptname: retrieves the XSEDE CA certs package on the web"
  echo "and installs them locally into '$CERTSDIR'."
  echo "You must have write permissions on and be able to 'cd' into the certificates"
  echo "directory for this script to work."
}

parm1="$1"; shift

if [[ "$parm1" =~ .*help.* ]]; then
  print_instructions
  exit 0
fi

# checks the result of the last command that was run, and if that failed,
# then this complains and exits from bash.  the function parameters are
# used as the message to print as a complaint.
#   freely copied from feisty meow codebase, given Apache ASL license.  --CAK
function check_result()
{
  if [ $? -ne 0 ]; then
    echo -e "failed on: $*"
    exit 1
  fi
}

# create a nice temporary directory to work in.
scratchdir="$(mktemp -d "/tmp/certs-check.${USER}.XXXXXX")"
echo "Working in temporary directory '$scratchdir'"

# go there.
pushd $scratchdir &>/dev/null
check_result "change into to the newly created temporary directory"

# get the current set of certificates.
echo "Getting the XSEDE CA certificates."
wget https://software.xsede.org/security/xsede-certs.tar.gz &>ca_cert_wget.log
check_result "getting the xsede certs package"

echo "Unpacking the XSEDE CA certificates."
tar -xf xsede-certs.tar.gz &>untarring.log
check_result "unpacking the xsede certs"

echo "Synching the new XSEDE CA certificates with '$CERTSDIR'."
rsync -lav certificates/* "$CERTSDIR" &>rsyncing.log
check_result "synching into the certificates directory.  do you have write permissions on '$CERTSDIR'?"

echo "Finding any extraneous files in '$CERTSDIR'."
\ls -1 certificates | sort >new_certs_contents.txt
\ls -1 "$CERTSDIR" | sort >old_certs_contents.txt

if [ ! -s new_certs_contents.txt -o ! -s old_certs_contents.txt ]; then
  echo Something unexpected went wrong in listing the new or old cert directories.
  exit 1
fi

comm -13 new_certs_contents.txt old_certs_contents.txt >items_only_in_etc.txt
check_result "finding items only in '$CERTSDIR'"

#debugging info
#filelist="$(cat items_only_in_etc.txt)"
#echo "removing items only in '$CERTSDIR':"
#cat "$filelist"

# remove the extraneous older files.
if [ -s items_only_in_etc.txt ]; then
  echo "Cleaning out extraneous files in '$CERTSDIR'"
  pushd "$CERTSDIR" &>/dev/null
  \rm -f $(cat "${scratchdir}/items_only_in_etc.txt")
  check_result "cleaning out extraneous files in '$CERTSDIR'"
  popd &>/dev/null
fi

# this fetches the CRL certificates for the CAs.  fetch-crl must be in the path.
echo "Fetching CRL files for the CA certificates in '$CERTSDIR'"
fetch-crl
check_result "fetching the CRLs for the CA certificates; is fetch-crl installed on the path?"

popd &>/dev/null

# clean up when script is done.
rm -rf "$scratchdir"

echo "Successfully finished updating the XSEDE CA Certs and CRL files."


