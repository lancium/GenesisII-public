#!/bin/bash

#### !!!!
# this script is out of date.
# it bootstraps a second container as a different user, but it currently requires
# manual intervention to set that up.
# the grid-bootstrap-backup script sets up a second container as same user, and may be more
# useful to expand on than this script.
#### !!!!

#
# bootstraps a small grid with no connectivity to other grids and sets up some default
# users, as well as a "normal" account from the command line parameters.
# this version expects to be able to use two containers, both of which should already
# have been started with a null configuration (user dir).
#
# Author: Chris Koeritz
# Author: Vanamala Venkataswamy
#

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_ROOT" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

##############

# constants used in script...

# the number of slots configured on each BES resource.
if [ -z "$QUEUE_SLOTS" ]; then
  QUEUE_SLOTS=4
fi

##############

new_admin_name="$1"; shift
new_password="$1"; shift
new_group="$1"; shift
primary_hostname="$1"; shift
secondary_hostname="$1"; shift
secondary_port="$1"; shift

if [ -z "$new_admin_name" -o -z "$new_password" -o -z "$new_group" -o -z "$primary_hostname" \
    -o -z "$secondary_hostname" -o -z "$secondary_port" ]; then
  echo "This script requires 4 parameters for the new grid setup:"
  echo "  $(basename $0) {username} {password} {group} {primary-host} {secondary-host} {port}"
  echo "Where the {username} will be created with the {password} and added"
  echo "to the {group}.  The {group} is created if it did not exist.  The {primary-"
  echo "host} will hold the RNS namespace root, and the {secondary-host} on {port}"
  echo "will be set up as a BES container and queue resource."
  echo "Note that the full path to the main user and group should be given, such as:"
  echo "  /users/ted   or   /groups/controllers"
  exit 1
fi

##############

# begin the active install steps.

echo "[$(date)]"

bootstrap_grid

get_root_privileges

##############

if [ "$BOOTSTRAP_LOC" != "$CONTAINERS_LOC/$primary_hostname" ]; then
  echo "Adding container alias '$primary_hostname' for bootstrap..."
  grid_chk ln $BOOTSTRAP_LOC "$CONTAINERS_LOC/$primary_hostname"
fi
# allow everyone to read the container's top-level.
grid_chk chmod "$CONTAINERS_LOC/$primary_hostname" +r --everyone

# one thing the bootstrap does not do is give us a queues directory.
echo "Creating top-level queues folder..."
grid mkdir $QUEUES_LOC
grid_chk chmod $QUEUES_LOC +r --everyone

echo "Adding container alias '$secondary_hostname' for secondary..."
#hmmm: eventually do not always count on localhost.  we may want to bootstrap a real second container on another host also.
grid_chk ln "--service-url=https://localhost:$secondary_port/axis/services/VCGRContainerPortType" "$CONTAINERS_LOC/$secondary_hostname"
# allow everyone to read.
grid_chk chmod "$CONTAINERS_LOC/$secondary_hostname" +r --everyone

# make an admin user and turn it into a grid god.
###hmmm: these are out of synch with better version in bootstrap single.
echo "Creating administrative user..."
create_user $USERS_LOC/admin admin $GROUPS_LOC/uva-idp-group
give_administrative_privileges $USERS_LOC/admin ${primary_hostname}
give_administrative_privileges $USERS_LOC/admin ${secondary_hostname}

create_queue "$QUEUES_LOC/${secondary_hostname}-queue" "$CONTAINERS_LOC/$secondary_hostname" "$new_admin_name" "$new_group"

# we also want to set up a BES to run on our container.
create_BES "$CONTAINERS_LOC/$primary_hostname" "$new_admin_name" "$QUEUE_PATH" "$new_group"

give_queue_a_resource "$QUEUES_LOC/${secondary_hostname}-queue" "$BES_CONTAINERS_LOC/${primary_hostname}-bes" $QUEUE_SLOTS

##############

# by here, the basic infrastructure of the grid itself should be established.

# drop our keystore login so we can be the admin user.
# all of the rest of the configuration should work properly as the admin account.
grid_chk logout --all
grid_chk login --username=admin --password=admin

bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$new_admin_name" "$new_password" "$new_group" "$HOMES_LOC" "$(dirname "$new_admin_name")"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user '$new_admin_name'."
  exit 1
fi

bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERS_LOC/test1" "test1" "$GROUPS_LOC/uva-idp-group" "$HOMES_LOC" "$USERS_LOC"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user 'test1'."
  exit 1
fi

bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERS_LOC/test2" "test2" "$GROUPS_LOC/uva-idp-group" "$HOMES_LOC" "$USERS_LOC"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user 'test2'."
  exit 1
fi

##############

# we would have bailed if there were an error during the bootstrap process.
echo "Successful bootstrap of primary on '$primary_hostname' with secondary '$secondary_hostname'."
exit 0

