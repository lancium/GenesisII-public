@echo off

set "ifErr=set foundErr=1&(if errorlevel 0 if not errorlevel 1 set foundErr=)&if defined foundErr"

if [%1] == [] goto :badparms
set drive="%1"

rem echo drive is set to %drive%

net use %drive%: \\127.0.0.1\grid
%ifErr% goto :failure
goto :success

:badparms
echo This script requires a drive letter on which to mount the GFFS SMB service.
set errorlevel=1
goto :exit

:failure
echo The GFFS SMB service could not be mounted on drive %drive%
set errorlevel=1
goto :exit

:success
echo The GFFS SMB service has been mounted successfully on drive %drive%

:exit
