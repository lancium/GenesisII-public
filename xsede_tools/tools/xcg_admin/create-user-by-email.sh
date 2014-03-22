#!/bin/bash

# establishes a user with appropriate rights to the grid resources configured in the
# input file (e.g., the queue, etc.).
# this assumes that an admin user is already logged in; that user must be able to chmod
# grid queues, home folders, and so forth.
#
# Author: Vanamala Venkataswamy
# Author: Chris Koeritz

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tests.sh before testing.; exit 3; fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

##############

while getopts 'e:p:g:h:u:c:' OPTION
do
case $OPTION in
e)      email=$OPTARG
        domain=(`echo $email|awk -F@ '{print $2}'|sed -n 1'p' | tr '.' '\n'`)

	new_username=(`echo $email|awk -F@ '{print $1}'`)
        ;;
h)      home_dir=$OPTARG
        ;;
p)     	new_password=$OPTARG 
        ;;
g)      new_group=$OPTARG
        ;;
u)      user_dir=$OPTARG
        ;;
c)      container_path=$OPTARG
        ;;
?)      printf "Usage: %s [-e email] [-p password] [-g group-name] [-h /home] [-u /users] \n" $(basename $0) >&2
        exit 2
        ;;
esac
done

#### Construct the home for new user
home_path=$home_dir
user_path=$user_dir
for((i=${#domain[@]}-1; i>=0; i--))
do
	home_path=$home_path"/${domain[$i]}"
	user_path=$user_path"/${domain[$i]}"
done

if [ -z "$email" -o -z "$new_password" -o -z "$new_group" -o -z "$home_dir" -o -z "$container_path" -o -z "$user_dir" ]; then
  echo "This script requires these parameters for preparing a user:"
  echo "  $(basename $0) {email} {password} {home dirctory} {group} {user-path} {container path}"
  echo "Where the {username} will be created with the {password} and added"
  echo "to the {group}.  The group is created if not already existent."
  echo "The user's home folder will reside under {home-path} and will be named"
  echo "the same as the base user name."
  echo "Note that the full path to the user and group should be given, such as:"
  echo "  /users/ted   or   /groups/controllers"
  exit 1
fi

##############

grid_chk mkdir -p $user_path
grid_chk mkdir -p $home_path/$new_username

new_home_path=$home_dir
new_user_path=$user_dir
for((i=${#domain[@]}-1; i>=0; i--))
do
	new_home_path=$new_home_path"/${domain[$i]}"
	new_user_path=$new_user_path"/${domain[$i]}"
	grid_chk chmod $new_home_path +rx --everyone
	grid_chk chmod $new_user_path +rx --everyone
done

grid_chk script local:./create_one_user_email.xml "$container_path" "$email" "$new_username" "$new_password" "$user_path" "$home_path" "$(basename "$new_group")" "$new_group"

# we would have bailed if there were an error during the creation process.
echo "Successful creation of user '$new_username' in group '$new_group'."
exit 0

