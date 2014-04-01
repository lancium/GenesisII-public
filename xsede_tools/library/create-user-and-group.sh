#!/bin/bash

# establishes a user with appropriate rights to the grid resources configured in the
# input file (e.g., the queue, etc.).
# this assumes that an admin user is already logged in; that user must be able to chmod
# grid queues, home folders, and so forth.
#
# Author: Chris Koeritz
# Author: Vanamala Venkataswamy

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

##############

new_username="$1"; shift
new_password="$1"; shift
new_group="$1"; shift
home_path="$1"; shift

if [ -z "$new_username" -o -z "$new_password" -o -z "$new_group" -o -z "$home_path" ]; then
  echo
  echo "This script requires these parameters for preparing a user:"
  echo
  echo "  $(basename $0) {user-rns} {password} {group-rns} {home-top}"
  echo
  echo "Where the {user-rns} will be created with the {password} and added"
  echo "to the {group-rns}.  The group is created if not already existent."
  echo "The user's home folder will reside under {home-top} and will be named"
  echo "the same as the base of the user's name (e.g. /users/joe would get"
  echo "a home folder in {home-top}/joe)."
  echo
  echo "Note that the full path to the user and group should be given, such as:"
  echo "  /users/ted  or  /groups/controllers"
  echo
  echo "Here is an example command for the XSEDE namespace:"
  echo "    $(basename $0) /users/xsede.org/tester xyzzy \\"
  echo "        /groups/xsede.org/gffs-amie /home/xsede.org"
  echo
  exit 1
fi

##############
 
grid script "local:'$XSEDE_TEST_ROOT/library/create_one_user.xml'" "$CONTAINERPATH" "$(basename $new_username)" "$new_username" "$new_password" "$(basename "$new_group")" "$new_group" "$home_path" "$(dirname "$new_username")"
check_if_failed user creation for $new_username

# we would have bailed if there were an error during the creation process.
echo "Successful creation of user '$new_username' in group '$new_group'."
exit 0

