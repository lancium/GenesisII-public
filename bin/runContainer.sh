#!/bin/sh

oldpwd=$(\pwd)
cd "/home/dev/GenesisII"
java_app="/home/dev/GenesisII/jre/bin/java"
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

# must not be used in main trunk, since the path to yjp would have to be present on everyone's computer.
PROFILERFLAGS="-agentpath:/usr/local/yjp-2016.02/bin/linux-x86-64/libyjpagent.so"

#$PROFILERFLAGS 
exec "$java_app" -Xms256M -Xmx2G $DEBUGFLAGS -classpath "/home/dev/GenesisII/bundles/org.apache.commons.logging_1.1.1.v201101211721.jar:/home/dev/GenesisII/ext/*:/home/dev/GenesisII/generated/*:/home/dev/GenesisII/lib:/home/dev/GenesisII/ext/axis/axis.jar" "-Dlog4j.configuration=build.container.log4j.properties" "-Djava.library.path=/home/dev/GenesisII/jni-libs/lin64" "-Dedu.virginia.vcgr.genii.install-base-dir=/home/dev/GenesisII" "-javaagent:/home/dev/GenesisII/ext/SizeOf.jar" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "/home/dev/GenesisII/ext/genii-container-application.properties" "$@"

cd "$oldpwd"

