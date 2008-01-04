@echo off
REM usage is connect-new-client.bat <deployment name> <net connect URL>

REM set install directory - by default, re-written by izpack (or you can set manually to test)
set _INC_GENII_INSTALL_DIR=$INSTALL_PATH
set _INC_LOCAL_JAVA_DIR=%_INC_GENII_INSTALL_DIR%\Java\windows-i586\jre

REM set vars for inputs
set _DEPLOYMENT_NAME=%1
set _CONNECT_URL=%2

if ""%_DEPLOYMENT_NAME%""=="""" goto emptydeployname
goto checkconnecturl
:emptydeployname
echo "Invalid tool usage.  <deployment name> arg is empty"
goto usage_exit

:checkconnecturl
if ""%_CONNECT_URL%""=="""" goto emptyconnecturl
goto setdeploydir
:emptyconnecturl
echo "Invalid tool usage.  <connect url> arg is empty"
goto usage_exit

:setdeploydir
set _DEPLOY_DIR=%_INC_GENII_INSTALL_DIR%\deployments\%_DEPLOYMENT_NAME%

REM copy java library stuff to allow big certs
REM echo copying Java files to enable large key sizes to %JAVA_HOME%\lib\security\
REM copy "%_INC_GENII_INSTALL_DIR%\ext\Java\lib\security\*.*" "%JAVA_HOME%\jre\lib\security\"

REM connect to net
echo connecting to %_CONNECT_URL%...
call "%_INC_GENII_INSTALL_DIR%\grid.bat" connect "%_CONNECT_URL%" "%_DEPLOY_DIR%"
