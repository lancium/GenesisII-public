#!/bin/bash

# Copy RPM, TAR, and JKS files to Production repositories

# establish all the variables needed...

HERE="$( \cd "$(\dirname "$0")" && \pwd )"

PKG=genesis2-xsede
#GRID=xsede

REPO_HOSTNAME=software.xsede.org
#DEV_PKG_BASE=/local/software.xsede.org/development/$PKG
#DEV_RPM_BASE=/local/software.xsede.org/development/repo
PROD_RPM_BASE=/local/software.xsede.org/production/repo
TEST_OPTION=""
#CMD=$(basename $0)

USAGE="This program will copy RPMs from the XSEDE YUM repository for development into\nthe production repository.\nIt supports the following flags:\n\t-h help\n\t-u username on repository\nIt also takes two arguments, the version number and release number for\nthe Genesis II GFFS release that should be copied."

# process command line arguments...

while getopts "thu:l:" opt; do
  case $opt in
    t) TEST_OPTION="-t"
       PROD_RPM_BASE="/local/software.xsede.org/testing/repo"
      ;;
    u) REPO_HOSTNAME="$OPTARG@$REPO_HOSTNAME"
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

# scoot past the options we consumed.
shift $((OPTIND-1))

VER="$1"; shift
REL="$1"; shift

#echo "got ver '$VER' and rel '$REL'"

if [ -z "$VER" -o -z "$REL" ]; then
  echo -e $USAGE >&2
  echo
  echo The version or release number was not specified on the command line.
  exit 1
fi

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

