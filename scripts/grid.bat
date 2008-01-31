@ECHO OFF

set _GRID_GENII_INSTALL_DIR=$INSTALL_PATH
set _GRID_LOCAL_JAVA_DIR=%_GRID_GENII_INSTALL_DIR%\Java\windows-i586\jre

"%_GRID_LOCAL_JAVA_DIR%\bin\java.exe" -Xms32M -Xmx128M -classpath "$INSTALL_PATH\ext\bouncycastle\bcprov-jdk15-133.jar;$INSTALL_PATH\lib\GenesisII-security.jar;$INSTALL_PATH\lib\morgan-utilities.jar;$INSTALL_PATH\lib;$INSTALL_PATH\security;$INSTALL_PATH\lib\GenesisII-client.jar" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=$INSTALL_PATH" org.morgan.util.launcher.Launcher "$INSTALL_PATH\jar-desc.xml" edu.virginia.vcgr.genii.client.cmd.Driver %*

set _GRID_GENII_INSTALL_DIR=
set _GRID_LOCAL_JAVA_DIR=



