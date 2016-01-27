

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo "you do not have the GENII_INSTALL_DIR variable set.  cannot clean up."
  exit 1
fi

# goes to a particular directory passed as parameter 1, and then removes all
# the parameters after that from that directory.
function push_whack_pop()
{
  local dir="$1"; shift

  pushd "$dir" &>/dev/null
  rm -rf $* &>/dev/null
  popd &>/dev/null
}

push_whack_pop $GENII_INSTALL_DIR container.properties  linux-cmd-wrapper  current.version  current.deployment  jar-desc.xml  set_gffs_vars  context.xml.lock  context.xml  
push_whack_pop $GENII_INSTALL_DIR/deployments/default context.xml
push_whack_pop $GENII_INSTALL_DIR/deployments/default/security admin.cer  signing-cert.pfx-base.pfx  signing-cert.pfx  signing-cert.cer  owner.cer  admin.pfx  tls-cert.cer  trusted.pfx  tls-cert.pfx  
push_whack_pop $GENII_INSTALL_DIR/deployments/default/security/default-owners admin.cer  owner.cer  
push_whack_pop $GENII_INSTALL_DIR/deployments/default/configuration namespace.properties bootstrap.xml
push_whack_pop $GENII_INSTALL_DIR/ext genii-client-application.properties  genii-base-application.properties  genii-container-application.properties  genii-certtool-application.properties  
push_whack_pop $GENII_INSTALL_DIR/libraries/gffs-security/trunk id.p12
push_whack_pop $GENII_INSTALL_DIR/lib container.properties  production.client.log4j.properties  client.properties  export.properties  genesisII.client.log4j.properties  build.client.log4j.properties  gffs.exports  production.container.log4j.properties  genesisII.container.log4j.properties  build.container.log4j.properties  

push_whack_pop $GENII_INSTALL_DIR bin bin.ant bin.eclipse /deployments backup /deployments/default services derby.log dpages generated unit-test-reports
push_whack_pop $GENII_INSTALL_DIR/libraries/CmdLineManipulator/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/DPage/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/FSViewII/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/GeniiJSDL/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/GeniiJSDL/trunk/doc api
push_whack_pop $GENII_INSTALL_DIR/libraries/GeniiProcessMgmt/trunk bin.ant api bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/GeniiProcessMgmt/trunk/jni generated-include
push_whack_pop $GENII_INSTALL_DIR/libraries/gffs-basics/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/gffs-security/trunk bin.ant bin.eclipse unit-test-reports
push_whack_pop $GENII_INSTALL_DIR/libraries/gffs-structure/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/gffs-webservices/trunk bin.ant bin.eclipse codegen dpages genned-obj genned-src services wsdd
push_whack_pop $GENII_INSTALL_DIR/libraries/MacOSXSwing/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/libraries/MNaming/trunk bin.ant bin.eclipse
push_whack_pop $GENII_INSTALL_DIR/webapps/axis/WEB-INF attachments

push_whack_pop $GENII_INSTALL_DIR/deployments backup
push_whack_pop $GENII_INSTALL_DIR/deployments/default services

