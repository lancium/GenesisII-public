@ECHO OFF

set _SC_GENII_INSTALL_DIR=$INSTALL_PATH
set _SC_LOCAL_JAVA_DIR=%_SC_GENII_INSTALL_DIR%\Java\windows-i586\jre

set APPLICATION_CLASS=%1%
shift

"%_SC_LOCAL_JAVA_DIR%\bin\java.exe" -Xms32M -Xmx128M -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-watcher.jar" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=$INSTALL_PATH" edu.virginia.vcgr.appwatcher.ApplicationLauncher "--application-class=%APPLICATION_CLASS%" "$INSTALL_PATH\ApplicationWatcher\genii-simple-application.properties" %1 %2 %3 %4 %5 %6 %7 %8 %9

set _SC_GENII_INSTALL_DIR=
set _SC_LOCAL_JAVA_DIR=

