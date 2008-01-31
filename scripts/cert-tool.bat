@ECHO OFF
set _CT_GENII_INSTALL_DIR=$INSTALL_PATH
set _CT_LOCAL_JAVA_DIR=%_CT_GENII_INSTALL_DIR%\Java\windows-i586\jre

"%_CT_LOCAL_JAVA_DIR%\bin\java.exe" -classpath "$INSTALL_PATH\ext\bouncycastle\bcprov-jdk15-133.jar;$INSTALL_PATH\lib\GenesisII-security.jar;$INSTALL_PATH\lib\morgan-utilities.jar;$INSTALL_PATH\lib;$INSTALL_PATH\security;$INSTALL_PATH\lib\GenesisII-client.jar" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" org.morgan.util.launcher.Launcher "$INSTALL_PATH\jar-desc.xml" edu.virginia.vcgr.genii.client.security.x509.CertTool %*

set _CT_GENII_INSTALL_DIR=
set _CT_LOCAL_JAVA_DIR=
