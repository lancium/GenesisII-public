#!/bin/bash

# Updates the in-grid copy of the certificates from the official XSEDE storage
# location of /etc/grid-security/certificates.

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo "This script uses the GENII_INSTALL_DIR variable to find the local Genesis II"
  echo "installation.  Please ensure that the variable is set."
  exit 1
fi
if [ ! -d "$GENII_INSTALL_DIR" -o ! -f "$GENII_INSTALL_DIR/grid" ]; then
  echo "The folder pointed at by \$GENII_INSTALL_DIR does not seem to be a valid"
  echo "installation of Genesis II: '$GENII_INSTALL_DIR'"
  echo "Please install the Genesis II client and set the GENII_INSTALL_DIR variable"
  echo "to point at it."
  exit 1
fi

function permfail()
{
  echo "An operation on the grid certificates folder at:"
  echo "'$gridcertsdir'"
  echo "failed.  This probably indicates that your user credentials do not give"
  echo "you sufficient permissions on the folder.  Please ensure that you are logged"
  echo "into the grid.  If the operation is failing while you are logged in, then"
  echo "please contact the grid administrator for help in gaining access to the"
  echo "folder.  The specific action that failed is:"
}

xsede_certs=/etc/grid-security/certificates

if [ ! -d "$xsede_certs" ]; then
  echo "The official storage location for XSEDE certificates does not seem to exist."
  echo "The folder should be found at: $xsede_certs"
  echo "Please download and install the XSEDE certificates.  To ensure CRL checking"
  echo "can be done, please also install and run the fetch-crl tool as sudo."
  exit 1
fi

outdir="$(mktemp -d /tmp/certs-update.XXXXXX)"

cp -R "$xsede_certs" "$outdir"

pushd "$outdir" &>/dev/null

newcertfile="$HOME/grid-certificates-$(date +%Y-%m-%d-%H%M).tar.gz"

tar -czf "$newcertfile" certificates
if [ $? -ne 0 ]; then
  echo "An error occurred while archiving the xsede certificates directory from"
  echo "our copy of it at '$outdir'.  Bailing out."
  exit 1
fi

popd &>/dev/null

# clean up temporary directory.
\rm -rf "$outdir"

gridcertsdir="/etc/grid-security/certificates"

tempoutfile="$(mktemp /tmp/lsgridcert.XXXXXX)"
# test that directory exists.  remove blank lines in output.
"$GENII_INSTALL_DIR/grid" ls "$gridcertsdir" 2>/dev/null >"$tempoutfile"  
if [ $? -ne 0 ]; then
  # the directory doesn't seem to exist yet so we'll create it.
  echo "The grid certificates directory does not exist; trying to create it."
  "$GENII_INSTALL_DIR/grid" mkdir -p "$gridcertsdir"
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem creating the in-grid certificates directory."
    exit 1
  fi

  # get the lines again.
  "$GENII_INSTALL_DIR/grid" ls "$gridcertsdir" 2>/dev/null >"$tempoutfile"  
  if [ $? -ne 0 ]; then
    permfail
    echo "Failed to list the in-grid certificates directory after creating it."
    exit 1
  fi
fi

# count how many items are there in the directory.
lines="$(sed -e '/^$/d' <"$tempoutfile" | wc | awk '{print $1}')"
echo line count of file is $lines
if [ $lines -eq 0 ]; then
  echo "An unexpected problem occurred when listing the in-grid certificates"
  echo "directory.  There were zero lines of output but there should always be"
  echo "at least one line.  Please inspect the folder:"
  echo "'$gridcertsdir'"
  exit 1
fi
if [ $lines -gt 1 ]; then
  "$GENII_INSTALL_DIR/grid" rm "$gridcertsdir/*"
  if [ $? -ne 0 ]; then
    permfail
    echo "Failed to clear the grid certificates directory of existing content."
    exit 1
  fi
fi

# copy new cert file into place in grid.
"$GENII_INSTALL_DIR/grid" cp local:"$newcertfile" "$gridcertsdir"
if [ $? -ne 0 ]; then
  permfail
  echo "Failed to copy up the new grid certificates package."
  exit 1
fi

echo "The new grid certificates package has been loaded into the grid..."
"$GENII_INSTALL_DIR/grid" ls "$gridcertsdir"

# clean up the local copy of the certs package.
\rm "$newcertfile"


