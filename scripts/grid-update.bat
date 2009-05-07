@ECHO OFF

set _GRID_GENII_INSTALL_DIR=$INSTALL_PATH
set _GRID_LOCAL_JAVA_DIR=%_GRID_GENII_INSTALL_DIR%\Java\windows-i586\jre

"%_GRID_LOCAL_JAVA_DIR%\bin\java.exe" -Xms32M -Xmx512M -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-manager.jar" -Dedu.virginia.vcgr.appwatcher.update-frequency=604800000 "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-lib" "-Dedu.virginia.vcgr.genii.install-base-dir=$INSTALL_PATH" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "$INSTALL_PATH\ApplicationWatcher\genii-update-application.properties" %*

set _GRID_GENII_INSTALL_DIR=
set _GRID_LOCAL_JAVA_DIR=
