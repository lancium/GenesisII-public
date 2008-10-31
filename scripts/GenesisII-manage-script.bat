@echo off

GOTO :InstallService

:InstallService
echo "Installing Genesis II Service"
ext\JavaServiceWrapper\bin\wrapper.exe -i "$INSTALL_PATH\ext\JavaServiceWrapper\conf\runContainer.conf" wrapper.ntservice.password.prompt=TRUE "set.LOCAL_JAVA_DIR=$INSTALL_PATH\Java\windows-i586\jre" "set.USER_NAME_STRING=%USERDOMAIN%\%USERNAME%" "set.GENII_INSTALL_DIR=$INSTALL_PATH"
if not errorlevel 1 GOTO :GrantServiceRights

echo "Unable to install new service."
pause
exit 1

:GrantServiceRights
echo "Granting Logon-as-Service right to %USERDOMAIN%\%USERNAME%"
ext\WindowsResourceKits\Tools\ntrights.exe +r SeServiceLogonRight -u "%USERDOMAIN%\%USERNAME%"
if not errorlevel 1 GOTO :StartService

echo "Unable to grant user rights to %USERDOMAIN%\%USERNAME%."
pause
exit 1

:StartService
echo "Starting the Genesis II Container Service"
net start "Genesis II Container"
if not errorlevel 1 GOTO :Finish

echo "Unable to start Genesis II Container."
pause
exit 1

:Finish
exit 0
