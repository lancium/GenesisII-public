#!/bin/bash

# This script modifies ACLS so that the grid user (i.e., the unix user who
# is running a container), can acquire access to a directory and its contents.
# It does not modify standard unix file permisions, and it expects that the
# grid user already has X permissions through the entire path up to the
# specified directory (i.e it does not modify any permissions of anything
# above it).
#
# Author: Mike Saravo
# Mods: Chris Koeritz (pared down to a script we can use in testing).

if [ $# -ne 3 ]; then
  echo -e "\nUSAGE:  $(basename $0) {username} {directory} {mask}"
  echo -e "\nThis script will set ACLs for the {username} to the {mask} on the"
  echo "{directory}, which will allow the {username}'s container to create exports"
  echo "based on the {directory}."
  echo -e "\nExample:\n  $(basename $0) fred /home/fred/grid_export rwx"
  echo -e "\nNote: currently, only an 'rwx' mask is recommended."
  exit 1
fi

grantee="$1"; shift
  # the user that permissions should be set for.  this is usually different
  # than the user running the script, who must already have full control
  # over the path.
dirpath="$(readlink -f $1)"; shift
  # where we will be changing the ACLs.  we get the directory, follow symbolic links,
  # and turn into a full path.
mask_desired="$1"; shift
  # the permissions to grant to the user.

# ensure that directory exists.
if [ ! -d "$dirpath" ]; then
  echo "The directory $dirpath must exist."
  exit 1
fi 

# Set ACLS recurssively on directory.
setfacl -R -m u:$grantee:$mask_desired "$dirpath"
if [ $? -ne 0 ]; then
  echo "Failed to setfacl on the existing path: $dirpath"
  exit 1
fi

# Set default ACLs recursively so future files are created with same acl,
# allowing grantee the desired permission mask, but keeping the current user's
# full access to files/directories.
setfacl -R -d --set u:$grantee:$mask_desired,u:$USER:rwx "$dirpath"
if [ $? -ne 0 ]; then
  echo "Failed to setfacl for new items on path: $dirpath"
  exit 1
fi

echo "Permissions succesfully modified"
echo "Consider setting up an export by running the following command in a grid client:"
echo -e "\n  export --create {ContainerX}/Services/LightWeightExportPortType $dirpath"
echo -e "\nThe export can be removed by running the command:"
echo -e "\n  export --quit $dirpath"
echo -e "\nNotes:\n(1) Removing the export does not change the ACLs on the host machine."
echo "(2) ls -l may not list correct permissions after acls are set.  Use getfacl"
echo "    instead to view permissions."
 
