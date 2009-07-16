@ECHO OFF

set "OLD_JAVA_HOME=%JAVA_HOME%"
set "OLD_PATH=%PATH%"

set "JAVA_HOME=${installer:sys.preferredJre}"
set "PATH=%PATH%;${installer:sys.installationDir}\jni-lib;%JAVA_HOME%\bin\client"

"${installer:sys.installationDir}\GenesisIFSServer.exe"

set "JAVA_HOME=%OLD_JAVA_HOME%"
set "PATH=%OLD_PATH%"
