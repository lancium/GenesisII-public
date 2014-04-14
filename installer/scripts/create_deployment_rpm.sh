#!/bin/bash

# This script will create a genesis2 gffs deployment package in RPM format.
# The user must already have created the deployment itself.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR

depdir="$1"; shift
packname="$1"; shift

# fix this if the version is increased.
CURRENT_PACKAGE="${packname}-deployment-2.7"

function print_instructions()
{
  echo "This script will create an RPM deployment package that overrides a GFFS"
  echo "installation's default grid.  This script requires two parameters; a"
  echo "directory that contains a valid grid deployment configuration and a name"
  echo "for the deployment override RPM to be generated.  The deployment directory"
  echo "should point at the top of the deployment to be packaged.  For example, if"
  echo "one's deployments tree looks like:"
  echo
  echo "folder = /home/fred/my_deployments"
  echo "+--xcg3"
  echo "  +--configuration"
  echo "    |--cservices"
  echo "  +--security"
  echo "    |--default-owners"
  echo "    |--myproxy-certs"
  echo "    |--trusted-certificates"
  echo
  echo "Then the folder to pass to this script is:"
  echo "  /home/fred/my_deployments/xcg3"
  echo "and one might run a full command with these parameters:"
  echo "  bash $(basename $0) $HOME/my_deployments/xcg3 xcg3-grid"
  echo
  echo "Note: it is important to start with a clean $HOME/rpmbuild directory"
  echo "before running this script."
}

if [ -z "$depdir" -o -z "$packname" ]; then
  print_instructions
  exit 1
fi

if [ ! -d "$depdir" ]; then
  print_instructions
  echo
  echo "The directory specified does not seem to exist: '$depdir'"
  exit 1
fi

if [ ! -d "$depdir/security" ]; then
  print_instructions
  echo
  echo "The directory does not have a security folder: '$depdir'"
  echo "This is probably not a valid deployment."
  exit 1
fi

if [ ! -d "$depdir/configuration" ]; then
  print_instructions
  echo
  echo "The directory does not have a configuration folder: '$depdir'"
  echo "This is probably not a valid deployment."
  exit 1
fi

echo Looking for a context file in XML format...
ls "$depdir/"*context.xml
if [ $? -ne 0 ]; then
  print_instructions
  echo
  echo "The directory does not have any files ending in 'context.xml'."
  echo "This is probably not a valid deployment."
  exit 1
fi
context_xml="$(basename $(ls "$depdir/"*context.xml) )"
echo calculated context xml as $context_xml

# create the package file that we will use.
depname="$(basename $depdir)"
echo "Deployment being packaged is called: $depname"
mkdir -p "$HOME/rpmbuild/SOURCES" 2>/dev/null
topdir="$HOME/rpmbuild/SOURCES/tempbuild"
rm -rf "$topdir" 2>/dev/null
stuffdir="$topdir/deployments"
mkdir -p "$stuffdir"
cp -R "$depdir" "$stuffdir"
pushd "$topdir"
if [ $? -ne 0 ]; then
  echo "There was a failure changing to the temporary dir: $topdir"
  exit 1
fi

# create a suitable container properties replacement.
echo -e "\
edu.virginia.vcgr.genii.container.deployment-name=$depname\n\
edu.virginia.vcgr.genii.gridInitCommand=\"local:/opt/genesis2/deployments/$depname/$context_xml\" \"$depname\"\n\
" >deployments/container.properties.${packname}

# create the container.properties update script, since we cannot overwrite an existing file
# installed by a different rpm.
echo -e "\
#!/bin/bash\n\
echo This script replaces the installed container.properties file with the\n\
echo $packname deployment override version of the file.  This requires a sudo\n\
echo login at the prompt.\n\
sudo cp /opt/genesis2/container.properties /opt/genesis2/container.properties.old\n\
sudo cp /opt/genesis2/deployments/container.properties.${packname} /opt/genesis2/container.properties\n\
echo If there were no errors, then the copy succeeded and the deployment override\n\
echo is ready to use.\n\
" >deployments/install_${packname}.sh
chmod a+rx deployments/install_${packname}.sh

# our source package of goods for the rpm building process.
sourcefile="$HOME/rpmbuild/SOURCES/${packname}-deployment.tar.gz"

# zip up the source package for the rpm.
tar -czf "$sourcefile" "deployments"
if [ $? -ne 0 ]; then
  echo "There was a failure creating the deployment archive in: $sourcefile"
  exit 1
fi
popd

# generate a modified deployment spec file for the RPM with the chosen name.
modified_dep_spec="$(mktemp /tmp/deployment-2.7-1.spec.XXXXXX)"
cp "$WORKDIR/gen2deployment-2.7-1.spec" "$modified_dep_spec"
sed -i -e"s/REPLACENAME/${packname}/g" "$modified_dep_spec"

# now build the rpm using the spec.
rpmbuild --bb "$modified_dep_spec"
if [ $? -ne 0 ]; then
  echo "There was a failure building the RPM of the deployment."
  exit 1
fi

# clean up the spec file.
\rm -f "$modified_dep_spec"

# copy the generated deployment rpm file up to our standard location.
mkdir "$HOME/installer_products" 2>/dev/null
echo Copying your new deployment RPM into place...
cp -v $HOME/rpmbuild/RPMS/noarch/${packname}* $HOME/installer_products
if [ $? -ne 0 ]; then
  echo "There was a failure copying the RPM up to the installer products."
  exit 1
fi

echo "The deployment RPM was successfully created."

