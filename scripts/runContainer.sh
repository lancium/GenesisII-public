#!/bin/sh

oldpwd=$(\pwd)
cd "${installer:sys.installationDir}"
java_app="${installer:sys.installationDir}/jre/bin/java"
if [ ! -f "$java_app" ]; then
  java_app="$JAVA_HOME/bin/java"
  if [ ! -f "$java_app" ]; then
    java_app="$(which java)"
    if [ ! -f "$java_app" ]; then
      echo "ERROR: could not deduce location of java from search path, JAVA_HOME or"
      echo "the Genesis II GFFS installation directory."
      exit 1
    fi
  fi
fi

DEBUGFLAGS=""
if [ ! -z "$JAVA_DEBUG_PORT" ]; then
  DEBUGFLAGS="-Xdebug -agentlib:jdwp=transport=dt_socket,address=$JAVA_DEBUG_PORT,server=y,suspend=n"
fi

exec "$java_app" -Xms256M -Xmx2G $DEBUGFLAGS -classpath "${installer:sys.installationDir}/bundles/org.apache.commons.logging_1.1.1.v201101211721.jar:${installer:sys.installationDir}/ext/*:${installer:sys.installationDir}/generated/*:${installer:sys.installationDir}/lib:${installer:sys.installationDir}/ext/axis/axis.jar" "-Dlog4j.configuration=%{LOG4JCONFIG}" "-Djava.library.path=${installer:sys.installationDir}/%{GENII_JNI_PATH}" "-Dedu.virginia.vcgr.genii.install-base-dir=${installer:sys.installationDir}" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "${installer:sys.installationDir}/ext/genii-container-application.properties" "$@"

cd "$oldpwd"

