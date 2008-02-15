@ECHO OFF

set "OLD_JAVA_HOME=%JAVA_HOME%"
set "OLD_PATH=%PATH%"

set "JAVA_HOME=$INSTALL_PATH\Java\windows-i586\jre"
set "PATH=%PATH%;$INSTALL_PATH\jni-lib;%JAVA_HOME%\bin\client"

"$INSTALL_PATH\GenesisIFSServer.exe"

set "JAVA_HOME=%OLD_JAVA_HOME%"
set "PATH=%OLD_PATH%"
