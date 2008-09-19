@echo off
REM usage is install-new-container.bat <deployment name> <container name> <net connect URL>

REM set install directory - by default, re-written by izpack (or you can set manually to test)
set _INC_GENII_INSTALL_DIR=$INSTALL_PATH
set _INC_LOCAL_JAVA_DIR=%_INC_GENII_INSTALL_DIR%\Java\windows-i586\jre

REM set vars for inputs
set _DEPLOYMENT_NAME=%1
set _CONTAINER_NAME=%2
set _CONNECT_URL=%3

if ""%_DEPLOYMENT_NAME%""=="""" goto emptydeployname
goto checkcontainername
:emptydeployname
echo "Invalid tool usage.  <deployment name> arg is empty"
goto usage_exit

:checkcontainername
if ""%_CONTAINER_NAME%""=="""" goto emptycontainername
goto checkconnecturl
:emptycontainername
echo "Invalid tool usage.  <container name> arg is empty"
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

rem figure out name of host
set _TEMP_FILE=tmpInstallContainer1.bat
call "%_INC_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _HOST_NAME=" > %_TEMP_FILE%
call "%_INC_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.GetHostName >> %_TEMP_FILE%
call "%_TEMP_FILE%"
del %_TEMP_FILE%
echo _HOST_NAME is now set to ....     %_HOST_NAME%

rem figure out port being used by container
set _TEMP_FILE=tmpInstallContainer2.bat
call "%_INC_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.MyEcho "set _HOST_PORT=" > %_TEMP_FILE%
call "%_INC_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.GetServerPort %_DEPLOYMENT_NAME% >> %_TEMP_FILE%
call "%_TEMP_FILE%"
del %_TEMP_FILE%

REM poke hole into firewall for new server
echo opening port %_HOST_PORT% in firewall for new server...
netsh firewall add portopening protocol=TCP name="%_CONTAINER_NAME%" port=%_HOST_PORT%

REM connect to net
echo connecting to %_CONNECT_URL%...
call "%_INC_GENII_INSTALL_DIR%\grid.bat" connect "%_CONNECT_URL%" "%_DEPLOYMENT_NAME%"

REM login as installer
echo Logging in as installer.  Please type in password at prompt...
call "%_INC_GENII_INSTALL_DIR%\grid.bat" login --no-gui "%_DEPLOY_DIR%\security\installer.pfx" --storetype=PKCS12 --pattern=Installer

REM generate new CA cert for new container
echo Generating certificate for new container...
set _CONTAINER_CERT_STORE=%_DEPLOY_DIR%\security\container.pfx
set _CERT_GEN_RNS_PATH=/etc/ContainerGroupCertGenerator
set _CONTAINER_CERT_ALIAS=VCGR Container
echo Creating container cert.  Command is %_INC_GENII_INSTALL_DIR%\grid.bat cert-generator --gen-cert %_CERT_GEN_RNS_PATH% "--ks-path=%_CONTAINER_CERT_STORE%" --ks-pword=container --ks-alias=%_CONTAINER_CERT_ALIAS% --cn=LOCAL_MACHINE_NAME
call "%_INC_GENII_INSTALL_DIR%\grid.bat" cert-generator --gen-cert "%_CERT_GEN_RNS_PATH%" "--ks-path=%_CONTAINER_CERT_STORE%" --ks-pword=container "--ks-alias=%_CONTAINER_CERT_ALIAS%" --cn=LOCAL_MACHINE_NAME --ou=VCGR --o=UVA --l=Charlottesville --c=US --st=VA

REM export .cer for container
echo Exporting container.cer.
del "%_DEPLOY_DIR%\security\container.cer"
"%_INC_LOCAL_JAVA_DIR%\bin\keytool" -export -file "%_DEPLOY_DIR%\security\container.cer" -keystore "%_CONTAINER_CERT_STORE%" -storepass container -alias "%_CONTAINER_CERT_ALIAS%" -storetype "PKCS12" 

REM logout as installer
echo Logging out as installer...
call "%_INC_GENII_INSTALL_DIR%\grid.bat" logout --all

REM install container as a service
echo Installing new container as a windows service...  Use command "%_INC_GENII_INSTALL_DIR%\ext\JavaServiceWrapper\bin\UninstallContainerWrapper.bat %_CONTAINER_NAME%" to remove container from being a windows service
call "%_INC_GENII_INSTALL_DIR%\InstallContainerWrapper.bat" "%_DEPLOYMENT_NAME%" %_CONTAINER_NAME%

REM Add Container to RNS space using _HOST_NAME
REM Security issues with RNS path?

REM sleep for a while waiting for server to come up
echo Sleeping for 60 seconds to wait for server to start...
call "%_INC_GENII_INSTALL_DIR%\simple-command.bat" edu.virginia.vcgr.genii.client.cmd.Sleep 60

REM setup rest of container stuff - use script.
echo setting up container using %_INC_GENII_INSTALL_DIR%\bootstrap-container.xml
call "%_INC_GENII_INSTALL_DIR%\grid.bat" script "DEPLOYMENT_NAME=%_DEPLOYMENT_NAME%" "GENII_INSTALL_DIR=%_INC_GENII_INSTALL_DIR%" "CONTAINER_NAME=%_HOST_NAME%" "CONTAINER_ADDR=https://localhost:%_HOST_PORT%" "%_INC_GENII_INSTALL_DIR%\bootstrap-container.xml"
 
echo "install-new-container finished successfully"
pause

goto end

:usage_exit
echo "Usage: install-new-container.bat <deployment name> <container name> <connect URL>"

:end
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
:mainEnd
