@ECHO OFF

set _TITLE=%1
shift

start "%_TITLE%" /wait "$INSTALL_PATH\windows-cmd-runner.bat" %*
