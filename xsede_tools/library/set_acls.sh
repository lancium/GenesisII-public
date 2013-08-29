#!/bin/bash

# This script modifies ACLS so that the grid user (i.e., the unix user who
# is running a container), can acquire access to a directory and its contents.
# It does not modify standard unix file permisions, and it expects that the
# grid user already has X permissions through the entire path up to the
# specified directory (i.e it does not modify any permissions of anything
# above it).
#
# Author: Mike Saravo (original version)
# Author: Chris Koeritz (pared down to a script we can use in testing, added
#         parameters for xsede usage).

function print_instructions()
{
  echo -e "\nUSAGE:  $(basename $0) [-R] {username} {directory} {mask}"
  echo -e "\nThis script can give permissions on the {directory} to the {username} specified"
  echo -e "via extended attributes.  The {username} will have the permissions in {mask}"
  echo "on the {directory}, which can allow the {username}'s GFFS container to create"
  echo "exports based on the {directory}.  If the -R flag is passed, then the perms"
  echo "will be set recursively (-R is optional, but must be passed prior to the"
  echo "username parameter).  Note that for a recursive operation, the contents in"
  echo "the {directory} will have sticky permissions; new contents will have the same"
  echo "{mask} permissions for the {username}."
  echo -e "\nExample:"
  echo -e "\n  $(basename $0) jan /home/fred rx"
  echo "     Set permissions on fred's home directory allowing jan to read it."
  echo -e "\n  $(basename $0) jan /home/fred/jan_store rwx"
  echo "     Assuming the prior command made fred's home readable, this allows jan full"
  echo "     permission on the jan_store subdirectory without giving her more rights"
  echo "     on fred's other files and directories.  This command also does not give"
  echo "     her permissions on any subdirectories beneath jan_store."
  echo -e "\n  $(basename $0) -R doug /home/fred/grid_export rwx"
  echo "     Recursively export the path with full permissions for the user doug"
  echo "     (assuming doug can already see into /home/fred)."
  echo
  echo "An export can be created later on the {directory} by running a command such as"
  echo "the following in a grid client:"
  echo -e "\n  export --create {ContainerX}/Services/LightWeightExportPortType \\"
  echo "    /home/fred/jan_store /home/xsede.org/userX/storage"
  echo -e "\nThe export can be removed by running the command:"
  echo -e "\n  export --quit /home/xsede.org/userX/storage"
  echo -e "\nNotes:\n(1) Removing the export does not change the ACLs on the host machine."
  echo "(2) ls -l may not list correct permissions after acls are set.  Use getfacl"
  echo "    instead to view permissions."
  echo
}

if [ $# -lt 3 ]; then
  print_instructions
  exit 1
fi

grantee="$1"; shift
  # the user that permissions should be set for.  this is usually different
  # than the user running the script, who must already have full control
  # over the path.

# empty recursion flag means do not use recursion.
recursive=
#hmmm: might be good to make this a getopts based script some day.
if [ "$grantee" == "-R" ]; then
  recursive="$grantee"
  # go back to the well for a new grantee.
  grantee="$1"; shift
fi

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

if [ ! -z "$recursive" ]; then
  # recursive mode is set.  make sure they are not hosing themselves.
  # we cannot check that the admin is doing this properly, but we can make
  # sure the user doesn't harm their security.
  homedir="$( \cd "$HOME" && \pwd )"
  targdir="$( \cd "$dirpath" && \pwd )"
  if [ "$homedir" == "$targdir" ]; then
    echo It is not safe to recursively set extended attributes on your home folder.
    echo This would allow user $grantee to have $mask_desired access to all assets
    echo in your HOME.  This script is intended to operate on subdirs under HOME
    echo 'or on less "sensitive" paths.'
    exit 1
  fi
fi

# make sure they are not trying to de-pants their crucial data folders.
forbidden_paths=(.globus .globusonline .secrets .ssh .subversion .ucc .Xauthority)
targdirbase="$(basename $dirpath)"
for i in ${forbidden_paths[@]}; do
  if [ "$targdirbase" == "$i" -o -e "$dirpath/$i" ]; then
    echo "The path includes a component ($i) that is explicitly blocked from"
    echo "being exported.  This is because that folder is known to contain private data"
    echo "(such as private keys or passwords)."
    exit 1
  fi
done

# Set ACLS on the directory.
setfacl $recursive -m u:$grantee:$mask_desired "$dirpath"
if [ $? -ne 0 ]; then
  echo "Failed to setfacl on the existing path: $dirpath"
  exit 1
fi

if [ ! -z "$recursive" ]; then
  # Set default ACLs recursively so future files are created with same acl,
  # allowing grantee the desired permission mask, but keeping the current
  # user's full access to the files and directories.
  setfacl $recursive -d --set u:$grantee:$mask_desired,u:$USER:rwx "$dirpath"
  if [ $? -ne 0 ]; then
    echo "Failed to setfacl for new items on path: $dirpath"
    exit 1
  fi
fi

echo "Permissions succesfully modified."

