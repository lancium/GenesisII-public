@ECHO OFF

set APPLICATION_CLASS=%1%
shift

"%JAVA_HOME%\bin\java.exe" -Xms32M -Xmx128M -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-manager.jar" "-Dlog4j.configuration=$CLIENT_LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-libs" "-Dedu.virginia.vcgr.genii.install-base-dir=$INSTALL_PATH" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "--application-class=%APPLICATION_CLASS%" "$INSTALL_PATH\ApplicationWatcher\genii-simple-application.properties" %1 %2 %3 %4 %5 %6 %7 %8 %9


