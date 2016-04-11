#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

#export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
echo loading gffs vars
  source ../../set_gffs_vars
fi

source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

echo genii binary is $GENII_BINARY_DIR


function create_unicore_bes()
{
  local unicore_url="$1"; shift
  local link_location="$1"; shift

echo unicore_url is $unicore_url
echo link_location is $link_location

  url_for_cert="$(echo $unicore_url|awk -F/ '{print $3}')"
  echo "Trying to download certificate from $url_for_cert"
  certificate_path=""
  which openssl > /dev/null
  if [ $? == 0 ]; then
    echo -n|openssl s_client -connect $url_for_cert 2> /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/${url_for_cert}.${USER}.cer
    if [ -s /tmp/${url_for_cert}.${USER}.cer ];then
      echo "Got certificate /tmp/${url_for_cert}.${USER}.cer"
      certificate_path=/tmp/${url_for_cert}.${USER}.cer
    else
      echo "Problem downloading certificate; please enter local path to unicore host certificate"
      read certificate_path
    fi
  else
    echo "Unable to find openssl; please enter local path to unicore host certificate"
    read certificate_path
  fi

  if [ ! -f "$certificate_path" ]; then
    echo "Unable to find file specified for certificate path: $certificate_path"
    exit 1
  fi

  echo "Linking new unicore bes in $link_location"
  full_url="${unicore_url}/services/BESFactory?res=default_bes_factory"
  "$GENII_BINARY_DIR/grid" mint-epr --link=$link_location --certificate-chain=local:$certificate_path "$full_url"
}

unicore_url="$1"; shift
link_location="$1"; shift

if [ -z "$unicore_url" -o -z "$link_location" ]; then
  echo "This script requires two parameters:"
  echo "(1) the path to a unicore BES in the form:"
  echo "  https://{hostname}:{port}/{SiteID}"
  echo "A testing u7 endpoint at SDSC might be, for example:"
  echo "  https://hpcdev-pub04.sdsc.edu:8082/TEST-HPCDEV"
  echo "and (2) the path to link the BES at in the GFFS grid."
  exit 1  
fi

create_unicore_bes "$unicore_url" "$link_location"

