@echo off
echo This batch script will register the GFFS SMB server for use on this host.
echo.

set "ifErr=set foundErr=1&(if errorlevel 0 if not errorlevel 1 set foundErr=)&if defined foundErr"

echo Stopping SMB service.
sc stop smb >nul 2>nul
rem ignore any error, since smb might not be running.

echo Configuring Windows SMB service to start on demand.
sc config smb start= demand
%ifErr% goto :failure

echo Configuring port forwarding for GFFS SMB server.
netsh interface portproxy add v4tov4 listenport=445 listenaddress=127.0.0.1 connectport=3333 connectaddress=%computername%
%ifErr% goto :failure

goto :success

:failure
echo The attempt to install GFFS SMB support has failed.
echo Possible reasons: This batch file must be invoked with administrative
echo privileges.  For example, start cmd.exe with admin rights and then run
echo the batch file from within that cmd.exe instance.
set errorlevel=1
goto :exit

:success
echo Successfully installed the GFFS SMB support.

:exit

