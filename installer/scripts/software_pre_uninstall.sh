#!/bin/bash

##############

# diagnostic noise to see variables and aliases.

#debug=
debug=true

if [ ! -z "$debug" ]; then
  myfile="$(basename $0)"
  targfile="/tmp/gffs-install-$USER-$myfile.log"
  # redirect output to the file.
  exec >"${targfile}" 2>&1
  echo "================= pwd output ================"
  pwd
  echo "================= set output ================"
  set
  echo "================= env output ================"
  env 
  echo "================= done ================"
fi

##############

# get the installation location.
GENII_INSTALL_DIR="$I4J_INSTALL_LOCATION"

\rm -f "$GENII_INSTALL_DIR/GFFSContainer"


