#!/bin/bash

# uploads the Genesis II GFFS RPMs to the YUM repository at XSEDE.

# establish all the variables needed...

HERE="$( \cd "$(\dirname "$0")" && \pwd )"

RPM_LOCATION="$HOME/installer_products"

TEMP_STORAGE="$HOME/rpms_to_transfer"

DEFAULT_FILE_PERMS="664"
DEFAULT_DIR_PERMS="775"
REPO_HOSTNAME=software.xsede.org
#DEV_PKG_BASE=/local/software.xsede.org/development/$PKG
DEV_RPM_BASE=/local/software.xsede.org/development/repo

USAGE="This program will upload RPMs to the XSEDE YUM repository for development.\nIt supports the following flags:\n\t-h help\n\t-u username on repository\n\t-l path where local RPMs are located"

# process command line arguments...

while getopts "hu:l:" opt; do
  case $opt in
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

#PKG_SUBDIR=$PKG-$VER-$REL

# uploads the appropriate RPM to the repository location provided.  this takes four flags:
#   operating system name, operating system release, platform, and architecture.
# all of the field names are expected to be values known to yum.
function upload_rpm() {
  osname=$1
  osrel=$2
  platform=$3
  arch=$4

  # not supporting i386 arch currently.
  if [[ ${arch} != "x86_64" ]]; then
     return
  fi

  mkdir -p $TEMP_STORAGE/$osname/$osrel/$arch/
  mkdir -p $TEMP_STORAGE/$osname/$osrel/SRPMS/

  echo Copying RPMs for $osname/$osrel/$arch/
  cp -p $RPM_LOCATION/$PKG-$GRID-$VER-$REL.$arch.rpm $TEMP_STORAGE/$osname/$osrel/$arch
  cp -p $RPM_LOCATION/$PKG-$GRID-$VER-$REL.src.rpm $TEMP_STORAGE/$osname/$osrel/SRPMS
}

# actual processing starts here...

echo "======="
echo "Working on RPMS for Genesis II GFFS version $VER release $REL for grid '$GRID'"
echo "Uploading to XSEDE Development YUM repository: https://software.xsede.org/development/repo/"
echo "======="

if [ -e "$TEMP_STORAGE" ]; then
  \rm -rf "$TEMP_STORAGE"
fi

for targetarch in i386 x86_64; do
{
  platform=redhat
  osname=redhat
  for osrel in 6Server 5Server; do
    upload_rpm ${osname} ${osrel} ${platform} ${targetarch}
  done
  osname=centos
  for osrel in 5 6; do
    upload_rpm ${osname} ${osrel} ${platform} ${targetarch}
  done

  osname=sl
  for osrel in 5 6 5.5 6.1; do
    upload_rpm ${osname} ${osrel} ${platform} ${targetarch}
  done

  osname=fedora
  for osrel in 16 17 18 19; do
    upload_rpm ${osname} ${osrel} ${platform} ${targetarch}
  done

  osname=sles
  platform=suse
  for osrel in 11.2; do
    if [[ ${targetarch} == "x86_64" ]]; then
      upload_rpm ${osname} ${osrel} ${platform} ${targetarch}
    fi
  done
}
#end big arch loop
done

pushd $TEMP_STORAGE &>/dev/null
# set permissions on the local files.
find . -type f -exec chmod $DEFAULT_FILE_PERMS "{}" ';'
rsync -vz -rp . $REPO_HOSTNAME:$DEV_RPM_BASE/ 2>&1 | grep -v 'failed to set permission'

popd &>/dev/null

echo Rebuilding Yum repository at xsede...
ssh -t $REPO_HOSTNAME /soft/repo-mgmt/bin/rebuild_yum_repositories.sh development

