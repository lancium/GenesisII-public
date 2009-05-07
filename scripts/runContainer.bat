@ECHO OFF

set _RC_GENII_INSTALL_DIR=$INSTALL_PATH
set _RC_LOCAL_JAVA_DIR=%_RC_GENII_INSTALL_DIR%\Java\windows-i586\jre

"%_RC_LOCAL_JAVA_DIR%\bin\java.exe" -Xms16M -Xmx512M -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-manager.jar" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=$INSTALL_PATH" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "$INSTALL_PATH\ApplicationWatcher\genii-container-application.properties" %*

set _RC_GENII_INSTALL_DIR=
set _RC_LOCAL_JAVA_DIR=
