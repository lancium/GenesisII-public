
To build the interactive and RPM/DEB installers for GenesisII, try:

bash $GENII_INSTALL_DIR/xsede_tools/tools/installer/build_installer.sh X.config

where X.config is the name of a GenesisII installer configuration file.  If
you leave off 'X.config', then the script will list out all of the available
configuration files.

To build the RPM deployment package by itself, try:

rpmbuild --bb gen2_deployment.spec

Both processes create their output in $HOME/installer_products.
