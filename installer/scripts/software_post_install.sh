#!/bin/bash

# figure out where we got installed.

# restore important config files from backup.

# replace installer variables in files.




# testing code to get variable sets:
myfile="$(basename $0)"
targfile="/home/fred/installer-$myfile-dump.txt"
# redirect output to the file.
exec >"${targfile}" 2>&1
echo "================= pwd output ================"
pwd
echo "================= set output ================"
set
echo "================= env output ================"
env 
echo "================= done ================"
