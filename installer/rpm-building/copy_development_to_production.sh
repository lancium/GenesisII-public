#!/bin/bash

# Copy RPM, TAR, and JKS files to Production repositories

# establish all the variables needed...

HERE="$( \cd "$(\dirname "$0")" && \pwd )"

RPM_LOCATION="$HOME/installer_products"

DEFAULT_FILE_PERMS="664"
DEFAULT_DIR_PERMS="775"
REPO_HOSTNAME=software.xsede.org
DEV_PKG_BASE=/local/software.xsede.org/development/$PKG
DEV_RPM_BASE=/local/software.xsede.org/development/repo
PROD_RPM_BASE=/local/software.xsede.org/production/repo
PKG_SUBDIR=$PKG-$VER-$REL
TEST_OPTION=""
CMD=$(basename $0)

USAGE="This program will copy RPMs from the XSEDE YUM repository for development into\nthe production repository.\nIt supports the following flags:\n\t-h help\n\t-u username on repository\n\t-l path where local RPMs are located"

# process command line arguments...

while getopts "thu:l:" opt; do
  case $opt in
    t) TEST_OPTION="-t"
       PROD_RPM_BASE="/local/software.xsede.org/testing/repo"
      ;;
    u) REPO_HOSTNAME="$OPTARG@$REPO_HOSTNAME"
       ;;
    l) RPM_LOCATION="$OPTARG"
       ;;
    h) 
      echo -e $USAGE 
      exit 0
      ;;
    \?)
      echo -e $USAGE >&2
      exit 1
      ;;
  esac
done

# verify the variables we were given and build derived variables...

if [ ! -d $RPM_LOCATION ]; then
  echo -e $USAGE
  echo
  echo "The local path for the RPMS of: $RPM_LOCATION"
  echo "does not exist.  Please specify a different location where the RPMs"
  echo "can be found."
  exit 1
fi

VERFILE="$RPM_LOCATION/current.version"
DEPFILE="$RPM_LOCATION/current.deployment"

if [ ! -e $VERFILE ]; then
  echo -e $USAGE
  echo
  echo "The installers do not seem to have been built; the file:"
  echo "   $VERFILE"
  echo "must exist before running this script, and it is created automatically"
  echo "by the GFFS build_installer script."
  exit 1
fi

PKG=genesis2
VER=$(sed -n -e 's/genii.app-version=\(.*\)/\1/p' "$VERFILE")
REL=$(sed -n -e 's/genii.release-number=\(.*\)/\1/p' "$VERFILE")
GRID=$(sed -n -e 's/genii.simple-name=\(.*\)/\1/p' "$DEPFILE")

PKG_SUBDIR=$PKG-$VER-$REL

###########################################
# Sign Development RPMs
###########################################
echo "** Signing Development RPMs with XSEDE Software signature"

ssh -t $REPO_HOSTNAME /soft/repo-mgmt/bin/xsedesig_development_packages.sh $PKG $VER $REL

###########################################
# Copy from Development to Production YUM repositories
###########################################
echo "** Copying from Development to Production YUM repositories $PROD_RPM_BASE"

ssh -t $REPO_HOSTNAME /soft/repo-mgmt/bin/copy_to_production.sh ${TEST_OPTION} $PKG $VER $REL

