#!/bin/bash

#find genesis 
#patch container properties to point at this deployment.

#for client, wipe the genii user dir.
#for container, keep genii user dir, but what's that mean if it was a different grid???
#=> cannot worry about this so much?  they are hosing themselves if they switch the
#   grid out from under their container.


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

