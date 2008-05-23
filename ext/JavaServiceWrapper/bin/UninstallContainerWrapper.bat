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
set _UCW_GENII_INSTALL_DIR=$INSTALL_PATH
set _UCW_LOCAL_JAVA_DIR=%_UCW_GENII_INSTALL_DIR%\Java\windows-i586\jre

rem get user's data directory...

set _TEMP_FILE=tmpUninstallContainer.bat
"%_UCW_LOCAL_JAVA_DIR%\bin\java.exe" -classpath "%_UCW_GENII_INSTALL_DIR%\lib\GenesisII-client.jar" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _GENII_USER_DIR=" > %_TEMP_FILE%
"%_UCW_LOCAL_JAVA_DIR%\bin\java.exe" -classpath "%_UCW_GENII_INSTALL_DIR%\lib\GenesisII-client.jar" edu.virginia.vcgr.genii.client.cmd.GetUserDir >> %_TEMP_FILE%
call "%_TEMP_FILE%"
del %_TEMP_FILE%
echo _GENII_USER_DIR is now set to ....     %_GENII_USER_DIR%
rem set _GENII_USER_DIR=C:\Documents and Settings\jfk3w\.genesisII

:cname
rem check if container name arg was passed in
set _CONTAINER_NAME=%1
if not %_CONTAINER_NAME%=="" goto wrapper_path
echo You must specify container name.  Usage: UninstallContainerWrapper-NT.bat <container service name>
pause
goto :cleanup
rem
rem Find the application home.
rem
rem %~dp0 is location of current script under NT

:wrapper_path
set _CONTAINER_DIR=%_GENII_USER_DIR%\%_CONTAINER_NAME%
set _WRAPPER_PATH=%_UCW_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_EXE=%_WRAPPER_PATH%%_WRAPPER_BASE%.exe
echo %_WRAPPER_EXE%
if exist "%_WRAPPER_EXE%" goto conf
echo Unable to locate a Wrapper executable using any of the following names:
echo %_WRAPPER_PATH%%_WRAPPER_BASE%.exe
pause
goto :cleanup

rem
rem Find the config file.
rem
:conf
set _WRAPPER_CONF=%_UCW_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\conf\runContainer.conf
echo %_WRAPPER_CONF%
if exist "%_WRAPPER_CONF%" goto removeit
echo Cannot find path to wrapper configuration file %_WRAPPER_CONF%
pause
goto :cleanup


rem
rem Uninstall the Wrapper as an NT service.
rem
:removeit
set _GENII_CONTAINER_NAME=%_CONTAINER_NAME%
echo ""
echo "%_WRAPPER_EXE%" -r "%_WRAPPER_CONF%" "set.LOCAL_JAVA_DIR=%_UCW_LOCAL_JAVA_DIR%" "set.CONTAINER_NAME=%_CONTAINER_NAME%" "set.GENII_INSTALL_DIR=%_UCW_GENII_INSTALL_DIR%" "set.GENII_USER_DIR=%_CONTAINER_DIR%"
echo ""

"%_WRAPPER_EXE%" -r "%_WRAPPER_CONF%" "set.LOCAL_JAVA_DIR=%_UCW_LOCAL_JAVA_DIR%" "set.CONTAINER_NAME=%_CONTAINER_NAME%" "set.GENII_INSTALL_DIR=%_UCW_GENII_INSTALL_DIR%" "set.GENII_USER_DIR=%_CONTAINER_DIR%"
if not errorlevel 1 goto :cleanupdir
pause
goto cleanup

:cleanupdir
rem remove directory that we stored container data in...
if exist "%_CONTAINER_DIR%" goto removecontainerdir
goto cleanup

:removecontainerdir
echo Removing directory for container: %_CONTAINER_DIR%.
rmdir /S /Q "%_CONTAINER_DIR%"


:cleanup
echo Cleaning up environment variables
set _CONTAINER_NAME=
set _GENII_USER_DIR=
set _UCW_GENII_INSTALL_DIR=
set _UCW_LOCAL_JAVA_DIR=
