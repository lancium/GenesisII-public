@echo off
REM usage is initialize_new_net_security.bat <deployment path> <net root CA pass> <admin pass> <installer pass> <container pass> <trust store pass>
REM This scripts assumes that the other scripts is uses are in the same path as itself

REM Find path to this script and use as path to other tools
set _REALPATH=%~dp0
set BIN_DIR=%_REALPATH%

REM set DEPLOY_DIR from arg
set DEPLOY_DIR=%1
if ""%DEPLOY_DIR%""=="""" goto emptyDeployName
shift

set NET_ROOT_CA_PASS=%1
if ""%NET_ROOT_CA_PASS%""=="""" goto emptyNetRootCAPass
shift

set ADMIN_PASS=%1
if ""%ADMIN_PASS%""=="""" goto emptyAdminPass
shift

set INSTALLER_PASS=%1
if ""%INSTALLER_PASS%""=="""" goto emptyInstallerPass
shift

set CONTAINER_PASS=%1
if ""%CONTAINER_PASS%""=="""" goto emptyContainerPass
shift

set TRUSTED_PASS=%1
if ""%TRUSTED_PASS%""=="""" goto emptyTrustedPass
shift


REM make directory for deployment
mkdir %DEPLOY_DIR%

set NET_ROOT_CA_PATH="%DEPLOY_DIR%\net-root-ca.pfx"

REM make new net root cert (self signed)
echo Making net-root-ca cert and placing in store %DEPLOY_DIR%\security\net-root-ca.pfx
del "%DEPLOY_DIR%\security\net-root-ca.pfx"
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Net Root CA Cert" -output-storetype="PKCS12" -output-entry-pass="%NET_ROOT_CA_PASS%" -output-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -output-keystore-pass="%NET_ROOT_CA_PASS%" -output-alias="GenesisII Net Root CA Cert"
REM make new admin cert from net root cert
echo Making admin cert and placing in store %DEPLOY_DIR%\security\admin.pfx
del "%DEPLOY_DIR%\security\admin.pfx"
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Admin Cert" -output-storetype="PKCS12" -output-entry-pass="%ADMIN_PASS%" -output-keystore="%DEPLOY_DIR%\security\admin.pfx" -output-keystore-pass="%ADMIN_PASS%" -output-alias="GenesisII Admin Cert" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for admin
echo Exporting admin cert to %DEPLOY_DIR%\security\admin.cer
del "%DEPLOY_DIR%\security\admin.cer"
keytool -export -file "%DEPLOY_DIR%\security\admin.cer" -keystore "%DEPLOY_DIR%\security\admin.pfx" -storepass "%ADMIN_PASS%" -alias "GenesisII Admin Cert" -storetype "PKCS12" 

REM make new installer cert from net root cert
del "%DEPLOY_DIR%\security\installer.pfx"
echo Making installer cert and placing in store %DEPLOY_DIR%\security\installer.pfx
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Installer Cert" -output-storetype="PKCS12" -output-entry-pass="%INSTALLER_PASS%" -output-keystore="%DEPLOY_DIR%\security\installer.pfx" -output-keystore-pass="%INSTALLER_PASS%" -output-alias="GenesisII Installer Cert" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for installer
del "%DEPLOY_DIR%\security\installer.cer"
echo Exporting installer cert to %DEPLOY_DIR%\security\installer.cer
keytool -export -file "%DEPLOY_DIR%\security\installer.cer" -keystore "%DEPLOY_DIR%\security\installer.pfx" -storepass "%INSTALLER_PASS%" -alias "GenesisII Installer Cert" -storetype "PKCS12" 

REM create trusted.pfx to store trusted certs
del "%DEPLOY_DIR%\security\trusted.pfx"
echo Creating %DEPLOY_DIR%\security\trusted.pfx with net-root-ca in it
call %BIN_DIR%\cert-tool.bat import -output-storetype="PKCS12" -output-keystore="%DEPLOY_DIR%\security\trusted.pfx" -output-keystore-pass="%TRUSTED_PASS%" -output-alias="GenesisII Net Root CA Cert" -input-storetype="PKCS12" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM make new container cert from net root cert (for bootstrap container)
set _MACHINE_NAME=mater.cs.virginia.edu
del "%DEPLOY_DIR%\security\container.pfx"
echo Making container cert and placing in store %DEPLOY_DIR%\security\container.pfx
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=%_MACHINE_NAME%" -output-storetype="PKCS12" -output-entry-pass="%CONTAINER_PASS%" -output-keystore="%DEPLOY_DIR%\security\container.pfx" -output-keystore-pass="%CONTAINER_PASS%" -output-alias="VCGR Container" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for container
del "%DEPLOY_DIR%\security\container.cer"
echo Exporting container cert to %DEPLOY_DIR%\security\container.cer
keytool -export -file "%DEPLOY_DIR%\security\container.cer" -keystore "%DEPLOY_DIR%\security\container.pfx" -storepass "%CONTAINER_PASS%" -alias "VCGR Container" -storetype "PKCS12" 

goto end

:emptyNetRootCAPass
echo "Missing net root CA password argument"
goto end

:emptyDeployName
echo "Missing deployment name argument"
goto end

:emptyAdminPass
echo "Missing admin cert password"
goto end

:emptyInstallerPass
echo "Missing installer password argument"
goto end

:emptyContainerPass
echo "Missing container password argument"
goto end

:emptyTrustedPass
echo "Missing trust store password argument"
goto end

:end
set BIN_DIR=
set DEPLOY_DIR=
set NET_ROOT_CA_PASS=
set ADMIN_PASS=
set INSTALLER_PASS=
:mainEnd