#!/bin/bash

# Uploads the in-grid copy of the certificates from the official XSEDE storage
# location of /etc/grid-security/certificates.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

#if [ -z "$GENII_INSTALL_DIR" ]; then
#  echo "This script uses the GENII_INSTALL_DIR variable to find the local Genesis II"
#  echo "installation.  Please ensure that the variable is set."
#  exit 1
#fi
if [ ! -d "$GENII_INSTALL_DIR" -o ! -f "$GENII_BINARY_DIR/grid" ]; then
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

# the certificates held in the local filesystem.
xsede_certs=/etc/grid-security/certificates

if [ ! -d "$xsede_certs" ]; then
  echo "The official storage location for XSEDE certificates does not seem to exist."
  echo "The folder should be found at: $xsede_certs"
  echo "Please download and install the XSEDE certificates.  To ensure CRL checking"
  echo "can be done, please also install and run the fetch-crl tool as sudo."
  exit 1
fi

# the certificates package storage area in the grid.
gridcertsdir="/etc/grid-security/certificates"

# first we get out of that directory if we were possibly there.
"$GENII_BINARY_DIR/grid" cd /
if [ $? -ne 0 ]; then
  echo "There was a problem changing directories to the root.  Is the grid up?"
  exit 1
fi

# test that the directory exists.
tempoutfile="$(mktemp /tmp/lsgridcert.XXXXXX)"
"$GENII_BINARY_DIR/grid" ls "$gridcertsdir" | sed -e '/^$/d' 2>/dev/null >"$tempoutfile"  
   # sed removes any blank lines.
if [ ${PIPESTATUS[0]} -ne 0 ]; then
  # the directory doesn't seem to exist yet so we'll create it.
  echo "The grid certificates directory does not exist; trying to create it."
  "$GENII_BINARY_DIR/grid" mkdir -p "$gridcertsdir"
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem creating the in-grid certificates directory."
    exit 1
  fi
  # make the certificates hierarchy readable by everyone.
  "$GENII_BINARY_DIR/grid" chmod /etc/grid-security +r --everyone
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem chmodding the /etc/grid-security directory for everyone."
    exit 1
  fi
  "$GENII_BINARY_DIR/grid" chmod /etc/grid-security/certificates +r --everyone
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem chmodding the /etc/grid-security/certificates directory for everyone."
    exit 1
  fi
  # make the hierarchy writable by admins.
  "$GENII_BINARY_DIR/grid" chmod /etc/grid-security +wx /groups/xsede.org/gffs-admins
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem chmodding the /etc/grid-security directory for gffs-admins."
    exit 1
  fi
  "$GENII_BINARY_DIR/grid" chmod /etc/grid-security/certificates +wx /groups/xsede.org/gffs-admins
  if [ $? -ne 0 ]; then
    permfail
    echo "There was a problem chmodding the /etc/grid-security/certificates directory for gffs-admins"
    exit 1
  fi

  # get the lines again now that the directory exists, just to have consistent file.
  "$GENII_BINARY_DIR/grid" ls "$gridcertsdir" | sed -e '/^$/d' 2>/dev/null >"$tempoutfile"  
  if [ ${PIPESTATUS[0]} -ne 0 ]; then
    permfail
    echo "Failed to list the in-grid certificates directory after creating it."
    exit 1
  fi
fi

# count how many items are there in the directory.
lines="$(wc "$tempoutfile" | awk '{print $1}')"
#echo line count of file is $lines
if [ $lines -eq 0 ]; then
  permfail
  echo "There was a problem listing the in-grid certificates directory."
  rm "$tempoutfile"
  exit 1
fi

if [ $lines -ge 2 ]; then
  # here we know that there's an existing certificates package, so we will download it
  # for comparison.
  tempcertfile="$(mktemp /tmp/certspack-curr.XXXXXX)"
  tempcertdir="$(mktemp -d /tmp/certs-existing.XXXXXX)"
  
  existingfile="$(tail -n 1 "$tempoutfile")"
  rm "$tempoutfile"
  "$GENII_BINARY_DIR/grid" cp "$gridcertsdir/$existingfile" "local:$tempcertfile"
  if [ $? -ne 0 ]; then
    echo Failed to copy existing certificates package for comparison.
    exit 1
  fi

  pushd "$tempcertdir" &>/dev/null
  tar -xf "$tempcertfile"
  if [ $? -ne 0 ]; then
    echo Failed to unpack existing certificates package for comparison.
    exit 1
  fi
  popd &>/dev/null
  diff -r "$xsede_certs" "$tempcertdir/certificates" &>/dev/null
  retval=$?
  # clean up before possibly bailing.
  rm -rf "$tempcertfile" "$tempcertdir"
  if [ $retval -ne 0 ]; then
    echo "Seeing differences in current xsede certs from existing package; will repackage."
  else
    echo "Current xsede certs are identical to existing package; will not repackage."
    exit 0
  fi
fi

# by here, we've decided to go ahead and make a new package.

# tar up a copy of the certificates.
certcopy="$(mktemp -d /tmp/certcopy.XXXXXX)"
mkdir "$certcopy/certificates"
cp "$xsede_certs"/* "$certcopy/certificates"
if [ $? -ne 0 ]; then
  echo "An error occurred while copying the xsede certificates directory from"
  echo "the official location at '$xsede_certs'.  Bailing out."
  exit 1
fi

pushd "$certcopy" &>/dev/null
newcertfile="$HOME/grid-certificates-$(date +%Y-%m-%d-%H%M).tar.gz"
tar -czf "$newcertfile" certificates
if [ $? -ne 0 ]; then
  echo "An error occurred while archiving the xsede certificates directory from"
  echo "our copy at '$certcopy'.  Bailing out."
  rm -rf "$certcopy"
  exit 1
fi
rm -rf "$certcopy"
popd &>/dev/null

if [ $lines -gt 1 ]; then
  "$GENII_BINARY_DIR/grid" rm "$gridcertsdir/*"
  if [ $? -ne 0 ]; then
    permfail
    echo "Failed to clear the grid certificates directory of existing content."
    exit 1
  fi
fi

# copy new cert file into place in grid.
"$GENII_BINARY_DIR/grid" cp local:"$newcertfile" "$gridcertsdir"
if [ $? -ne 0 ]; then
  permfail
  echo "Failed to copy up the new grid certificates package."
  exit 1
fi

# make all the contents of the certs dir readable.
"$GENII_BINARY_DIR/grid" chmod "/etc/grid-security/certificates/*" +r --everyone
if [ $? -ne 0 ]; then
  permfail
  echo "There was a problem chmodding the contents of the $gridcertsdir directory for everyone."
  exit 1
fi
# give admins full control over all of these files.
"$GENII_BINARY_DIR/grid" chmod "/etc/grid-security/certificates/*" +wx /groups/xsede.org/gffs-admins
if [ $? -ne 0 ]; then
  permfail
  echo "There was a problem chmodding the contents of the $gridcertsdir directory for gffs-admins."
  exit 1
fi

echo "The new grid certificates package has been loaded into the grid..."
"$GENII_BINARY_DIR/grid" ls "$gridcertsdir"

# clean up the local copy of the certs package.
\rm "$newcertfile"


