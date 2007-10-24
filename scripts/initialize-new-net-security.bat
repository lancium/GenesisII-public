@echo off
REM usage is initialize_new_net_security.bat <deployment path> <net root CA pass> <container group CA pass> <admin pass> <installer pass> <container pass> <trust store pass>
REM This scripts assumes that the other scripts is uses are in the same path as itself

REM Find path to this script and use as path to other tools
REM set _REALPATH=%~dp0

set _INNS_GENII_INSTALL_DIR=$INSTALL_PATH
set _INNS_LOCAL_JAVA_DIR=%_INNS_GENII_INSTALL_DIR%\Java\windows-i586\jre
set BIN_DIR=%_INNS_GENII_INSTALL_DIR%


REM set DEPLOY_DIR from arg
set DEPLOY_DIR=%1
if ""%DEPLOY_DIR%""=="""" goto emptyDeployPath
shift

set NET_ROOT_CA_PASS=%1
if ""%NET_ROOT_CA_PASS%""=="""" goto emptyNetRootCAPass
shift

set CONTAINER_GROUP_CA_PASS=%1
if ""%CONTAINER_GROUP_CA_PASS%""=="""" goto emptyContainerGroupCAPass
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

REM make sure directory for trusted certs exists
echo Creating %DEPLOY_DIR%\security\certs\trusted directory (if it does not exist)
mkdir "%DEPLOY_DIR%\security\certs\trusted"

set NET_ROOT_CA_PATH="%DEPLOY_DIR%\net-root-ca.pfx"

REM make new net root cert (self signed)
echo Making net-root-ca cert and placing in store %DEPLOY_DIR%\security\net-root-ca.pfx
del "%DEPLOY_DIR%\security\net-root-ca.pfx"
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Net Root CA Cert" -output-storetype="PKCS12" -output-entry-pass="%NET_ROOT_CA_PASS%" -output-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -output-keystore-pass="%NET_ROOT_CA_PASS%" -output-alias="GenesisII Net Root CA Cert"

REM export .cer for net-root-ca
echo Exporting net-root-ca cert to %DEPLOY_DIR%\security\net-root-ca.cer
del "%DEPLOY_DIR%\security\net-root-ca.cer"
"%_INNS_LOCAL_JAVA_DIR%\bin\keytool.exe" -export -file "%DEPLOY_DIR%\security\net-root-ca.cer" -keystore "%DEPLOY_DIR%\security\net-root-ca.pfx" -storepass "%%NET_ROOT_CA_PASS%%" -alias "GenesisII Net Root CA Cert" -storetype "PKCS12" 

REM copy net-root-cert cert to %DEPLOY_DIR%\security\certs\trusted
echo copying new net-root-cert.cer to %DEPLOY_DIR%\security\certs\trusted\net-root-cert.cer
del "%DEPLOY_DIR%\security\certs\trusted\net-root-cert.cer"
copy "%DEPLOY_DIR%\security\net-root-cert.cer" /B "%DEPLOY_DIR%\security\certs\trusted\net-root-cert.cer" /B

REM make new container group CA cert from net root cert
echo Making new container group CA cert and placing in store %DEPLOY_DIR%\security\containergrp.pfx
del "%DEPLOY_DIR%\security\containergrp.pfx"
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Container Group CA Cert" -output-storetype="PKCS12" -output-entry-pass="%CONTAINER_GROUP_CA_PASS%" -output-keystore="%DEPLOY_DIR%\security\containergrp.pfx" -output-keystore-pass="%CONTAINER_GROUP_CA_PASS%" -output-alias="GenesisII Container Group CA Cert" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for containergrp
echo Exporting containergrp cert to %DEPLOY_DIR%\security\containergrp.cer
del "%DEPLOY_DIR%\security\containergrp.cer"
"%_INNS_LOCAL_JAVA_DIR%\bin\keytool" -export -file "%DEPLOY_DIR%\security\containergrp.cer" -keystore "%DEPLOY_DIR%\security\containergrp.pfx" -storepass "%CONTAINER_GROUP_CA_PASS%" -alias "GenesisII Container Group CA Cert" -storetype "PKCS12" 

REM copy containergrp cert to %DEPLOY_DIR%\security\certs\groups
echo copying new containergrp.cer to %DEPLOY_DIR%\security\certs\groups\containergrp.cer
del "%DEPLOY_DIR%\security\certs\groups\containergrp.cer"
copy "%DEPLOY_DIR%\security\containergrp.cer" /B "%DEPLOY_DIR%\security\certs\groups\containergrp.cer" /B

REM make new admin cert from net root cert
echo Making admin cert and placing in store %DEPLOY_DIR%\security\admin.pfx
del "%DEPLOY_DIR%\security\admin.pfx"
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Admin Cert" -output-storetype="PKCS12" -output-entry-pass="%ADMIN_PASS%" -output-keystore="%DEPLOY_DIR%\security\admin.pfx" -output-keystore-pass="%ADMIN_PASS%" -output-alias="GenesisII Admin Cert" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for admin
echo Exporting admin cert to %DEPLOY_DIR%\security\admin.cer
del "%DEPLOY_DIR%\security\admin.cer"
"%_INNS_LOCAL_JAVA_DIR%\bin\keytool" -export -file "%DEPLOY_DIR%\security\admin.cer" -keystore "%DEPLOY_DIR%\security\admin.pfx" -storepass "%ADMIN_PASS%" -alias "GenesisII Admin Cert" -storetype "PKCS12" 
copy "%DEPLOY_DIR%\security\admin.cer" /B "%DEPLOY_DIR%\security\certs\users\admin.cer" /B

REM make new installer cert from net root cert
del "%DEPLOY_DIR%\security\installer.pfx"
echo Making installer cert and placing in store %DEPLOY_DIR%\security\installer.pfx
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=GenesisII Installer Cert" -output-storetype="PKCS12" -output-entry-pass="%INSTALLER_PASS%" -output-keystore="%DEPLOY_DIR%\security\installer.pfx" -output-keystore-pass="%INSTALLER_PASS%" -output-alias="GenesisII Installer Cert" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for installer
del "%DEPLOY_DIR%\security\installer.cer"
echo Exporting installer cert to %DEPLOY_DIR%\security\installer.cer
"%_INNS_LOCAL_JAVA_DIR%\bin\keytool" -export -file "%DEPLOY_DIR%\security\installer.cer" -keystore "%DEPLOY_DIR%\security\installer.pfx" -storepass "%INSTALLER_PASS%" -alias "GenesisII Installer Cert" -storetype "PKCS12" 

REM create trusted.pfx to store trusted certs
del "%DEPLOY_DIR%\security\trusted.pfx"
for %%CERT_FILE in ("%DEPLOY_DIR%\security\certs\trusted\*.cer") do call %BIN_DIR%\cert-tool.bat import -output-keystore="%DEPLOY_DIR%\security\trusted.pfx" -output-keystore-pass="%TRUSTED_PASS%" -base64-cert-file="%DEPLOY_DIR%\security\certs\trusted\%CERT_FILE"

REM echo Creating %DEPLOY_DIR%\security\trusted.pfx with net-root-ca in it
REM call %BIN_DIR%\cert-tool.bat import -output-storetype="PKCS12" -output-keystore="%DEPLOY_DIR%\security\trusted.pfx" -output-keystore-pass="%TRUSTED_PASS%" -output-alias="GenesisII Net Root CA Cert" -input-storetype="PKCS12" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM make new container cert from net root cert (for bootstrap container)
set _MACHINE_NAME=mater.cs.virginia.edu
del "%DEPLOY_DIR%\security\container.pfx"
echo Making container cert and placing in store %DEPLOY_DIR%\security\container.pfx
call %BIN_DIR%\cert-tool.bat gen -dn="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=%_MACHINE_NAME%" -output-storetype="PKCS12" -output-entry-pass="%CONTAINER_PASS%" -output-keystore="%DEPLOY_DIR%\security\container.pfx" -output-keystore-pass="%CONTAINER_PASS%" -output-alias="VCGR Container" -input-keystore="%DEPLOY_DIR%\security\net-root-ca.pfx" -input-keystore-pass="%NET_ROOT_CA_PASS%" -input-storetype="PKCS12" -input-entry-pass="%NET_ROOT_CA_PASS%" -input-alias="GenesisII Net Root CA Cert"

REM export .cer for container
del "%DEPLOY_DIR%\security\container.cer"
echo Exporting container cert to %DEPLOY_DIR%\security\container.cer
"%_INNS_LOCAL_JAVA_DIR%\bin\keytool" -export -file "%DEPLOY_DIR%\security\container.cer" -keystore "%DEPLOY_DIR%\security\container.pfx" -storepass "%CONTAINER_PASS%" -alias "VCGR Container" -storetype "PKCS12" 

goto end

:emptyNetRootCAPass
echo "Missing net root CA password argument"
goto end

:emptyContainerGroupCAPass
echo "Missing container group CA password argument"
goto end

:emptyDeployPath
echo "Missing deployment path argument"
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
set REALPATH=
set BIN_DIR=
set DEPLOY_DIR=
set NET_ROOT_CA_PASS=
set CONTAINER_GROUP_CA_PASS=
set ADMIN_PASS=
set INSTALLER_PASS=
set CONTAINER_PASS=
set TRUSTED_PASS=
set NET_ROOT_CA_PATH=
set _MACHINE_NAME=
set _INNS_GENII_INSTALL_DIR=
set _INNS_LOCAL_JAVA_DIR=

:mainEnd
