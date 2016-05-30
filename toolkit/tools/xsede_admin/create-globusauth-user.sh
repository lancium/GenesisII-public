#!/bin/bash

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

source "../../prepare_tools.sh" "../../prepare_tools.sh"
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"


# shows the parameter usage info.
function print_instructions()
{
#scavenge code for getting basename.

#hmmm: not done...
  echo This script requires 2 parameters, the container path where the GlobusAuth IDP will be created and the user name to create on that container.
}

# get the command line arguments.
containerpath="$1"; shift
username="$1"; shift

if [ -z "$containerpath" -o -z "$username" ]; then
  print_instructions
  exit 1
fi

# before we start, make sure this hasn't already been done.
fastgrid ping --eatfaults "/users/globus-auth/$username" &>/dev/null
if [ $? -eq 0 ]; then
  echo "The identity /users/globus-auth/$username already exists!"
  echo "This tool seems to have already been invoked.  If there are errors"
  echo "with $username logging in via globus, please inspect the"
  echo "identity to ensure it is set up properly (i.e. the globus-auth user"
  echo "has rights on the xsede.org user, and a link of the xsede.org user"
  echo "exists under the globus-auth user to cause automatic login.)"
  # special exit value in case scripts want to know that this was a different
  # kind of failure.
  exit 8
fi

# first verify that the xsede.org user exists; the globus auth support is
# predicated on being able to login to globus but then become the "real"
# xsede.org identity.
fastgrid ping --eatfaults "/users/xsede.org/$username" &>/dev/null
if [ $? -ne 0 ]; then
  echo The identity /users/xsede.org/$username does not exist yet.  This identity
  echo must exist before a Globus Auth identity can be created, since the Globus
  echo Auth identity essentially becomes linked with the /users/xsede.org identity.
  exit 1
fi

# now verify that the user entry is actually a kerberos port type underneath.
fastgrid ping --eatfaults "$containerpath/Services/KerbAuthnPortType/$username" &>/dev/null
if [ $? -ne 0 ]; then
  echo The identity /users/xsede.org/$username does not seem to be a kerberos based
  echo STS item.  We will skip it.
  # special exit code here too means not a failure quite, but we can't convert.
  exit 9
fi


# create the globus auth idp storage folder if it doesn't exist.
fastgrid ping --eatfaults "/users/globus-auth" &>/dev/null
if [ $? -ne 0 ]; then
  fastgrid mkdir "/users/globus-auth"
  check_if_failed "creation of /users/globus-auth hierarchy"
  fastgrid chmod "/users/globus-auth" +r --everyone
  check_if_failed "giving everyone permission to read globus auth dir"
fi

# then create the idp using idptool.
fastgrid idp --type=USER --validDuration=10years "$containerpath/Services/GlobusAuthnPortType" "$username"
check_if_failed "creating the globus auth idp on the container $containerpath"

# link the new idp name into the appropriate place in the grid.
fastgrid ln "$containerpath/Services/GlobusAuthnPortType/$username" "/users/globus-auth/$username"
check_if_failed "linking the new globus auth idp into the location: /users/globus-auth/$username"

# link the gffs-users group in so the pattern based acl can rely on myproxy connection tls.
fastgrid ln "/groups/xsede.org/gffs-users" "/users/globus-auth/$username/gffs-users"
check_if_failed "linking gffs-users group under the sts location: /users/globus-auth/$username/gffs-users"

# give the new idp permissions on the older kerberos based idp.
fastgrid chmod "/users/xsede.org/$username" +rx "/users/globus-auth/$username"
check_if_failed "giving rights on /users/xsede.org/$username for globus auth idp"

# make the new globus-auth id try to login to the kerberos id automatically.
fastgrid ln "/users/xsede.org/$username" "/users/globus-auth/$username/kerb-${username}" 
check_if_failed "linking xsede.org $username under globus auth idp for automatic login"


#need resolver addition also if we are supporting replication with globus auth.


echo "GlobusAuth STS successfully created for $username"


