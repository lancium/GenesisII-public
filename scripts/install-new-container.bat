REM usage is install-new-container.bat <deployment name> <container name> <net connect URL>

REM set install directory - by default, re-written by izpack (or you can set manually to test)
set _GENII_INSTALL_DIR=C:\Program Files\Genesis II

REM set vars for inputs
set _DEPLOYMENT_NAME=%1
set _CONTAINER_NAME=%2
set _CONNECT_URL=%3

if "%_DEPLOYMENT_NAME%"=="" goto emptydeployname
goto checkcontainername
:emptydeployname
echo "Invalid tool usage.  <deployment name> arg is empty"
goto usage_exit

:checkcontainername
if "%_CONTAINER_NAME%"=="" goto emptycontainername
goto checkconnecturl
:emptycontainername
echo "Invalid tool usage.  <container name> arg is empty"
goto usage_exit

:checkconnecturl
if "%_CONNECT_URL%"=="" goto emptyconnecturl
goto setdeploydir
:emptyconnecturl
echo "Invalid tool usage.  <connect url> arg is empty"
goto usage_exit

:setdeploydir
set _DEPLOY_DIR=%_GENII_INSTALL_DIR%\deployments\%_DEPLOYMENT_NAME%

rem figure out name of host
set _TEMP_FILE=tmpInstallContainer1.bat
call "%_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _HOST_NAME=" > %_TEMP_FILE%
call "%_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.GetHostName >> %_TEMP_FILE%
call "%_TEMP_FILE%"
REM del %_TEMP_FILE%
echo _HOST_NAME is now set to ....     %_HOST_NAME%

REM connect to net
echo connecting to %_CONNECT_URL%...
call "%_GENII_INSTALL_DIR%\grid.bat" connect "%_CONNECT_URL%" "%_DEPLOY_DIR%"

REM login as installer
echo Logging in as installer.  Please type in password at prompt...
call "%_GENII_INSTALL_DIR%\grid.bat" login --no-gui "--file=%_DEPLOY_DIR%\security\installer.pfx" --storetype=PKCS12 Installer

REM generate new CA cert for new container
echo Generating certificate for new container...
set _CONTAINER_CERT_STORE=%_DEPLOY_DIR%\security\container.pfx
set _CERT_GEN_RNS_PATH=/etc/NetRootCertGenerator
set _CONTAINER_CERT_ALIAS=VCGR Container
echo Creating container cert.  Command is %_GENII_INSTALL_DIR%\grid.bat cert-generator --gen-cert %_CERT_GEN_RNS_PATH% "--ks-path=%_CONTAINER_CERT_STORE%" --ks-pword=container --ks-alias=%_CONTAINER_CERT_ALIAS% --cn=LOCAL_MACHINE_IP
call "%_GENII_INSTALL_DIR%\grid.bat" cert-generator --gen-cert "%_CERT_GEN_RNS_PATH%" "--ks-path=%_CONTAINER_CERT_STORE%" --ks-pword=container "--ks-alias=%_CONTAINER_CERT_ALIAS%" --cn=LOCAL_MACHINE_IP

REM install container as a service
echo Installing new container as a windows service...  Use command "%_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\UninstallContainerWrapper.bat %_CONTAINER_NAME%" to remove container from being a windows service
call "%_GENII_INSTALL_DIR%\InstallContainerWrapper.bat" "%_DEPLOYMENT_NAME%" %_CONTAINER_NAME%

REM Add Container to RNS space using _HOST_NAME
REM Security issues with RNS path?
rem figure out port being used by container
set _TEMP_FILE=tmpInstallContainer2.bat
call "%_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _HOST_PORT=" > %_TEMP_FILE%
call "%_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.GetServerPort %_DEPLOYMENT_NAME% >> %_TEMP_FILE%
call "%_TEMP_FILE%"
REM del %_TEMP_FILE%
echo _HOST_PORT is now set to ....     %_HOST_PORT%
call "%_GENII_INSTALL_DIR%\grid.bat" ln "--service-url=https://localhost:%_HOST_PORT%/axis/services/VCGRContainerPortType" "/containers/%_HOST_NAME%"

REM logout as installer
echo Logging out as installer...
call "%_GENII_INSTALL_DIR%\grid.bat" logout

goto end

:usage_exit
echo Usage: install-new-container.bat <deployment name> <container name> <connect URL>

:end
set _GENII_INSTALL_DIR=
set _DEPLOYMENT_NAME=
set _CONTAINER_NAME=
set _DEPLOY_DIR=
set _CONTAINER_CERT_STORE=
set _CERT_GEN_RNS_PATH=
set _CONTAINER_CERT_ALIAS=
set _CONNECT_URL=
:mainEnd