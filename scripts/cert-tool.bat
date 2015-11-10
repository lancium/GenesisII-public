@ECHO OFF
"%JAVA_HOME%\bin\java.exe" -classpath "${installer:sys.installationDir}\bundles\org.apache.commons.logging_1.1.1.v201101211721.jar;${installer:sys.installationDir}\ext\*;${installer:sys.installationDir}\lib" "-Dlog4j.configuration=$CLIENT_LOG4JCONFIG" "-Djava.library.path=${installer:sys.installationDir}\jni-libs" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "${installer:sys.installationDir}\ext\genii-certtool-application.properties" %*

