#!/bin/sh

exec "$JAVA_HOME/bin/java" -Xms256M -Xmx512M -classpath "%{INSTALL_PATH}/lib:%{INSTALL_PATH}/ApplicationWatcher/app-manager.jar" "-Dlog4j.configuration=%{LOG4JCONFIG}" "-Djava.library.path=%{INSTALL_PATH}/%{GENII_JNI_PATH}" "-Dedu.virginia.vcgr.genii.install-base-dir=%{INSTALL_PATH}" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "%{INSTALL_PATH}/ApplicationWatcher/genii-container-application.properties" "$@"
