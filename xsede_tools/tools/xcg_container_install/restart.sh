#!/bin/bash

tmpfile=/tmp/tmpFile
crontab -l >$tmpfile
echo '*/5 * * * * $HOME/GenesisIIShell/XCGContainer start' >>$tmpfile
cat $tmpfile | crontab -

