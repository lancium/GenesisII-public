@ECHO OFF
set _TITLE=%1
shift

cd /d "${installer:sys.installationDir}"
start %_TITLE% /wait windows-cmd-runner.bat %~s1 %2 %3 %4 %5 %6 %7 %8 %9

