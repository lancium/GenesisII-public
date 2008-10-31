@echo off

REM set install directory - by default, re-written by izpack (or you can set manually to test)
set _INC_GENII_INSTALL_DIR=$INSTALL_PATH
set _INC_LOCAL_JAVA_DIR=%_INC_GENII_INSTALL_DIR%\Java\windows-i586\jre

REM install container as a service
echo Installing new container as a windows service...  Use command "%_INC_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\UninstallContainerWrapper.bat to remove container from being a windows service
call "%_INC_GENII_INSTALL_DIR%\InstallContainerWrapper.bat"

echo "install-new-container finished successfully"

set _INC_GENII_INSTALL_DIR=
set _DEPLOYMENT_NAME=
set _CONTAINER_NAME=
set _DEPLOY_DIR=
set _CONTAINER_CERT_STORE=
set _CERT_GEN_RNS_PATH=
set _CONTAINER_CERT_ALIAS=
set _CONNECT_URL=
set _TEMP_FILE=
set _HOST_PORT=
set _INC_LOCAL_JAVA_DIR=
