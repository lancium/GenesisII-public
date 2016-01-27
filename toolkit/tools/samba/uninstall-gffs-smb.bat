@echo off

set "ifErr=set foundErr=1&(if errorlevel 0 if not errorlevel 1 set foundErr=)&if defined foundErr"

echo configuring samba service to start automatically again.
sc config smb start= auto
%ifErr% echo There was a failure reconfiguring SMB service to start automatically.

echo removing port fowarding for gffs samba service
netsh interface portproxy delete v4tov4 listenport=445 listenaddress=127.0.0.1
%ifErr% echo There was a failure removing the port forward for GFFS SMB.

echo Done uninstalling GFFS SMB service.

