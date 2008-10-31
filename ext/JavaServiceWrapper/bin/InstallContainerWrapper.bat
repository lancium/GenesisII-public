@echo off
setlocal

rem Copyright (c) 1999, 2006 Tanuki Software Inc.
rem
rem Java Service Wrapper general NT service install script
rem

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
set _ICW_GENII_INSTALL_DIR=$INSTALL_PATH
set _ICW_LOCAL_JAVA_DIR=%_ICW_GENII_INSTALL_DIR%\Java\windows-i586\jre

rem get user's data directory...

set _TEMP_FILE=tmpInstallContainer.bat
"%_ICW_LOCAL_JAVA_DIR%\bin\java.exe" -classpath "%_ICW_GENII_INSTALL_DIR%\lib\GenesisII-client.jar" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _GENII_USER_DIR=" > %_TEMP_FILE%
"%_ICW_GENII_INSTALL_DIR%\grid.bat" GetUserDir >> %_TEMP_FILE%
call "%_TEMP_FILE%"
del %_TEMP_FILE%
echo _GENII_USER_DIR is now set to ....     %_GENII_USER_DIR%

:wrapper_path
set _CONTAINER_DIR=%_GENII_USER_DIR%
set _WRAPPER_PATH=%_ICW_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_EXE=%_WRAPPER_PATH%%_WRAPPER_BASE%.exe
if exist "%_WRAPPER_EXE%" goto conf
echo Unable to locate a Wrapper executable using any of the following names:
echo %_WRAPPER_PATH%%_WRAPPER_BASE%.exe
goto :cleanup

rem
rem Find the config file.
rem
:conf
set _WRAPPER_CONF=%_ICW_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\conf\runContainer.conf
echo %_WRAPPER_CONF%
if exist "%_WRAPPER_CONF%" goto user
echo Cannot find path to wrapper configuration file %_WRAPPER_CONF%
goto :cleanup


:user
rem figure out user to install as
set _USER_NAME_STRING=%USERDOMAIN%\%USERNAME%

rem create directory to store container data...
echo Creating directory for container: %_CONTAINER_DIR%.
mkdir "%_CONTAINER_DIR%"

echo You should install the windows services by running "%_WRAPPER_EXE%" -i "%_WRAPPER_CONF%" wrapper.ntservice.password.prompt=TRUE "set.LOCAL_JAVA_DIR=%_ICW_LOCAL_JAVA_DIR%" "set.USER_NAME_STRING=%_USER_NAME_STRING%" "set.DEPLOYMENT_NAME=%_DEPLOYMENT_NAME%" "set.CONTAINER_NAME=%_CONTAINER_NAME%" "set.GENII_INSTALL_DIR=%_ICW_GENII_INSTALL_DIR%" "set.GENII_USER_DIR=%_CONTAINER_DIR%"
goto :cleanup

:startit
rem try to give user logon as service rights
"%_ICW_GENII_INSTALL_DIR%\ext\WindowsResourceKits\Tools\ntrights.exe" +r SeServiceLogonRight -u %_USER_NAME_STRING%
net start "Genesis II Container %_CONTAINER_NAME%"

:cleanup
set _DEPLOYMENT_NAME=
set _CONTAINER_NAME=
set _GENII_USER_DIR=
set _ICW_GENII_INSTALL_DIR=
set _ICW_LOCAL_JAVA_DIR=
