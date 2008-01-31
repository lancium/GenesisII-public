#!/bin/sh

_RC_GENII_INSTALL_DIR="%{INSTALL_PATH}"
_RC_LOCAL_JAVA_DIR="$_RC_GENII_INSTALL_DIR/Java/linux-i586/jre"

exec "$_RC_LOCAL_JAVA_DIR/bin/java" -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -Xms16M -Xmx128M -classpath "%{INSTALL_PATH}/ext/bouncycastle/bcprov-jdk15-133.jar:%{INSTALL_PATH}/lib/GenesisII-security.jar:%{INSTALL_PATH}/lib/morgan-utilities.jar:%{INSTALL_PATH}/lib:%{INSTALL_PATH}/security" "-Dlog4j.configuration=%{LOG4JCONFIG}" "-Djava.library.path=%{INSTALL_PATH}/jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=%{INSTALL_PATH}" org.morgan.util.launcher.Launcher "%{INSTALL_PATH}/jserver-jar-desc.xml" edu.virginia.vcgr.ogrsh.server.OGRSHServer "$@"
