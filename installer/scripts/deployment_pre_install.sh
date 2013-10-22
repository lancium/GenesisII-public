#!/bin/bash


# stop container (shared code with gffs pre deploy)






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
