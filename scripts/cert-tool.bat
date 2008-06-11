@ECHO OFF
set _CT_GENII_INSTALL_DIR=$INSTALL_PATH
set _CT_LOCAL_JAVA_DIR=%_CT_GENII_INSTALL_DIR%\Java\windows-i586\jre

"%_CT_LOCAL_JAVA_DIR%\bin\java.exe" -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-watcher.jar" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" edu.virginia.vcgr.appwatcher.ApplicationLauncher "$INSTALL_PATH\ApplicationWatcher\genii-certtool-application.properties" %*

set _CT_GENII_INSTALL_DIR=
set _CT_LOCAL_JAVA_DIR=
