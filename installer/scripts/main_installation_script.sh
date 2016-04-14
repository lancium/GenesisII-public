#!/bin/bash

##############

# bootstrap our information about the installation, starting with where it
# resides.
export GENII_INSTALL_DIR="$1"; shift
export GENII_BINARY_DIR="$GENII_INSTALL_DIR/bin"

##############

# load our helper scripts.

if [ ! -f "$GENII_INSTALL_DIR/scripts/installation_helpers.sh" ]; then
  echo "The installation_helpers.sh script could not be located in the existing"
  echo "installation.  This is most likely because this install was created with"
  echo "the Genesisi v2.7.499 installer or earlier.  Please upgrade to the latest"
  echo "Genesis 2.7.500+ interactive installer before proceeding."
  exit 1
fi

source "$GENII_INSTALL_DIR/scripts/installation_helpers.sh"

##############

replace_compiler_variables "$GENII_INSTALL_DIR/RELEASE"
replace_compiler_variables "$GENII_INSTALL_DIR/lib/container.properties"
replace_compiler_variables "$GENII_INSTALL_DIR/lib/client.properties"

# new files for bin directory need to be copied from scripts.
if [ ! -f "$GENII_BINARY_DIR/gffschown" ]; then
  cp "$GENII_INSTALL_DIR/scripts/gffschown.template" "$GENII_BINARY_DIR/gffschown"
  chmod 755 "$GENII_BINARY_DIR/gffschown"
fi
if [ ! -f "$GENII_BINARY_DIR/proxyio.launcher" ]; then
  cp "$GENII_INSTALL_DIR/scripts/proxyio.launcher.template" "$GENII_BINARY_DIR/proxyio.launcher"
  chmod 755 "$GENII_BINARY_DIR/proxyio.launcher"
fi

replace_installdir_variables "$GENII_INSTALL_DIR"

##############

# all variable replacing in config files and scripts should be done by this point.

# make a link for the Container startup script.
rm -f "$GENII_BINARY_DIR/GFFSContainer"
ln -s "$GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/bin/GFFSContainer" "$GENII_BINARY_DIR/GFFSContainer"
chmod 755 "$GENII_BINARY_DIR/GFFSContainer"

# clean up some older files and directories.
\rm -rf "$GENII_INSTALL_DIR/ApplicationWatcher" "$GENII_INSTALL_DIR/XCGContainer" "$GENII_INSTALL_DIR/lib/gffs-container.jar" "$GENII_INSTALL_DIR/GFFSContainer" "$GENII_INSTALL_DIR/cert-tool" "$GENII_INSTALL_DIR/client-ui"

# set the permissions on our files properly.
find "$GENII_INSTALL_DIR" -type d -exec chmod -c a+rx "{}" ';' &>/dev/null
find "$GENII_INSTALL_DIR" -type f -exec chmod -c a+r "{}" ';' &>/dev/null
find "$GENII_INSTALL_DIR" -type f -iname "*.sh" -exec chmod -c a+rx "{}" ';' &>/dev/null
find "$GENII_INSTALL_DIR/JavaServiceWrapper" -type f -iname "wrap*" -exec chmod -c a+rx "{}" ';' &>/dev/null

# special case for linux 64 bit, to avoid the wrapper using 32 bit version.
archfound=$(arch)
if [ "x86_64" == "$archfound" -o "amd64" == "$archfound" ]; then
  \rm -f "$GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/bin/wrapper-linux-x86-32"
fi

##############

echo "Finished preparing installation for GenesisII GFFS."
exit 0


