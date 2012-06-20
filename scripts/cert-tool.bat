@ECHO OFF
"%JAVA_HOME%\bin\java.exe" -classpath "$INSTALL_PATH\lib;$INSTALL_PATH\ApplicationWatcher\app-manager.jar" "-Dlog4j.configuration=$CLIENT_LOG4JCONFIG" "-Djava.library.path=$INSTALL_PATH\jni-libs" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "$INSTALL_PATH\ApplicationWatcher\genii-certtool-application.properties" %*

