
####

To build the interactive and DEB installers for GenesisII, try:

  bash "$GENII_INSTALL_DIR/toolkit/tools/installer/build_installer.sh" X.config

where X.config is the name of a GenesisII installer configuration file.  If
you leave off 'X.config', then the script will list out all of the available
configuration files.

Creates the interactive install4j installers in $HOME/installer_products-{grid name}.

####

To build the RPM installation package, use the following:

  cd "$GENII_INSTALL_DIR/installer/rpm-building"
  bash build_64bit_rpm.sh X.config

The RPM is signed by the key defined in "$HOME/.rpmmacros".  This is an example
configuration for this file:
%_signature gpg
%_gpg_name  Chris Koeritz (XSEDE Key) <koeritz@virginia.edu>

Creates the source tarball, source RPM and installer RPM packages in
$HOME/installer_products-{grid name}.

####


