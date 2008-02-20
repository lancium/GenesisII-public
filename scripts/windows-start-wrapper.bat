@ECHO OFF
set _TITLE=%1
shift

cd /d "$INSTALL_PATH"
start %_TITLE% /wait windows-cmd-runner.bat %~s1 %2 %3 %4 %5 %6 %7 %8 %9

set _TITLE=
