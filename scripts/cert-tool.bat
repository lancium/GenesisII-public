@ECHO OFF

java -classpath "$INSTALL_PATH\ext\bouncycastle\bcprov-jdk15-133.jar;$INSTALL_PATH\lib\GenesisII-security.jar;$INSTALL_PATH\lib\morgan-utilities.jar;$INSTALL_PATH\lib;$INSTALL_PATH\security" -Dlog4j.configuration=genesisII.log4j.properties "-Djava.library.path=$INSTALL_PATH\jni-lib" org.morgan.util.launcher.Launcher "$INSTALL_PATH\jar-desc.xml" edu.virginia.vcgr.genii.client.security.x509.CertTool %*
