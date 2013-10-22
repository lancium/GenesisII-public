#!/bin/bash

# stop the existing container, if there is one.

# backup existing config files.



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
