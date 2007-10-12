REM usage is uninstall-container.bat <deployment name> <container name>

REM set install directory - by default, re-written by izpack (or you can set manually to test)
set _GENII_INSTALL_DIR=C:\Program Files\Genesis II

REM set vars for inputs
set _DEPLOYMENT_NAME=%1
set _CONTAINER_NAME=%2

if "%_DEPLOYMENT_NAME%"=="" goto emptydeployname
goto checkcontainername
:emptydeployname
echo "Invalid tool usage.  <deployment name> arg is empty"
goto usage_exit

:checkcontainername
if "%_CONTAINER_NAME%"=="" goto emptycontainername
goto setdeploydir
:emptycontainername
echo "Invalid tool usage.  <container name> arg is empty"
goto usage_exit

:setdeploydir
set _DEPLOY_DIR=%_GENII_INSTALL_DIR%\deployments\%_DEPLOYMENT_NAME%
set _CONTAINER_CERT_STORE=%_DEPLOY_DIR%\security\container.pfx

:douninstall

REM install container as a service
echo Uninstalling new container as a windows service...  Use command "%_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\UninstallContainerWrapper.bat %_CONTAINER_NAME%" to remove container from being a windows service
call "%_GENII_INSTALL_DIR%\UninstallContainerWrapper.bat" %_CONTAINER_NAME%

REM remove cert for container
echo Removing cert %_CONTAINER_CERT_STORE%
del "%_CONTAINER_CERT_STORE%"
goto end


:usage_exit
echo Usage: uninstall-container.bat <deployment name> <container name>

:end
set _GENII_INSTALL_DIR=
set _CONTAINER_NAME=
set _CONTAINER_CERT_STORE=
:mainEnd