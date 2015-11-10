@echo off

"%JAVA_HOME%\bin\java.exe" -Xms16M -Xmx512M -classpath "${installer:sys.installationDir}\bundles\org.apache.commons.logging_1.1.1.v201101211721.jar;${installer:sys.installationDir}\ext\*;${installer:sys.installationDir}\lib" "-Dlog4j.configuration=$LOG4JCONFIG" "-Djava.library.path=${installer:sys.installationDir}\%{GENII_JNI_PATH}" "-Dedu.virginia.vcgr.genii.install-base-dir=${installer:sys.installationDir}" edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher "${installer:sys.installationDir}\ext\genii-container-application.properties" %*

