#!/bin/bash

# bootstraps a small grid with no connectivity to other grids and sets up some default
# users, as well as a "normal" account from the command line parameters.
#
# Author: Chris Koeritz
# Author: Vanamala Venkataswamy

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

if [ -z "$new_admin_name" -o -z "$new_password" -o -z "$new_group" -o -z "$primary_hostname" ]; then
  echo "This script requires 4 parameters for the new grid setup:"
  echo "  $(basename $0) {username} {password} {group} {hostname}"
  echo "Where the {username} will be created with the {password} and added"
  echo "to the {group} (which is created if non-existent).  The host will"
  echo "also be set up as a BES container and queue resource using the {hostname}."
  echo "Note that the full path to the main user and group should be given, such as:"
  echo "  /users/ted   or   /groups/controllers"
  exit 1
fi

##############

# begin the active install steps.

echo "[$(date)]"

# we take a dim view of containers that are already started, given that this
# script is a no-nonsense single container grid builder; usually if the
# container is already running or there's an existing user directory, it's an
# oversight.
echo "Cleaning out user directory '$GENII_USER_DIR'"
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh
sleep 2
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh
\rm -rf "$GENII_USER_DIR" "$XSEDE_TEST_ROOT/"*.log "$XSEDE_TEST_ROOT/"*.log.*

if [ $NAMESPACE == 'xsede' ]; then
  echo Copying xsede namespace properties into place.
  cp $GENII_INSTALL_DIR/deployments/default/configuration/xsede-namespace.properties $GENII_INSTALL_DIR/deployments/$DEPLOYMENT_NAME/configuration/namespace.properties
elif [ $NAMESPACE == 'xcg' ]; then
  echo Copying xcg namespace properties into place.
  cp $GENII_INSTALL_DIR/deployments/default/configuration/xcg-namespace.properties $GENII_INSTALL_DIR/deployments/$DEPLOYMENT_NAME/configuration/namespace.properties
else
  echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
  exit 1
fi

##############

# add some variables needed for the certificate generators.
C=US
ST=Virginia
L=Charlottesville
O=GENIITEST
OU='Genesis II'

##############

# we will generate new certs unless told not to.  the default is to always
# regenerate, since we do not want any vulnerability in a simple bootstrap
# grid from using certs that might have just been hanging around or which were
# retrieved from the repository.
if [ -z "$REUSE_GRID_CERTIFICATES" ]; then
  create_grid_certificates
fi

launch_container_if_not_running "$DEPLOYMENT_NAME"

echo Startup log state...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

bootstrap_grid

echo After grid basic infrastructure created...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

get_root_privileges

##############

# basic creation of the admin user occurs here.
create_group "$new_group"
create_user "$new_admin_name" "$new_password" "$new_group"

# test logging in as the new id before doing anything else.
grid_chk logout --all
grid_chk login --username="$(basename $new_admin_name)" --password="$new_password"

# okay, if we're still here, the admin user works and we can go back to grid setup.
grid_chk logout --all
get_root_privileges

##############

# copy up the flag that shows this is not a useful, real, secure grid.
grid_chk cp local:$WORKDIR/a_bogus_grid.txt /
# and let everyone read that file.
grid_chk chmod /a_bogus_grid.txt +r --everyone

##############

# make a link for the bootstrap container to use the hostname we were given.
if [ "$BOOTSTRAP_LOC" != "$CONTAINERS_LOC/$primary_hostname" ]; then
  echo "Adding container alias '$primary_hostname' for bootstrap..."
  grid_chk ln $BOOTSTRAP_LOC "$CONTAINERS_LOC/$primary_hostname"
fi

# allow everyone to read the container's top-level.
grid_chk chmod "$CONTAINERS_LOC/$primary_hostname" +r --everyone

##############

# one thing the bootstrap does not do is give us a queues directory.
echo "Creating top-level queues folder..."
# we just run grid for the queues mkdir and don't check it; it didn't used to
# be created by the bootstrap, but it is now.  still creating it is just
# supporting older versions of the bootstrap.
grid mkdir $QUEUES_LOC
grid_chk chmod $QUEUES_LOC +r --everyone

##############

# create a queue and a BES that the queue can use for job submission.

create_queue "$QUEUE_PATH" "$CONTAINERS_LOC/$primary_hostname" "$new_admin_name" "$new_group"

create_BES "$CONTAINERS_LOC/$primary_hostname" "$new_admin_name" "$QUEUE_PATH" "$new_group"

give_queue_a_resource "$QUEUE_PATH" "$BES_CONTAINERS_LOC/${primary_hostname}-bes" $QUEUE_SLOTS

##############

# now power up that admin guy.
give_administrative_privileges "$new_admin_name" "${primary_hostname}"
# give admin total control over the group.
grid_chk chmod "$new_group" +rwx "$new_admin_name"
# give admin the right to administrate the container.
grid_chk chmod "$CONTAINERS_LOC/$primary_hostname" +rwx "$new_admin_name"

##############

# by here, the basic infrastructure of the grid itself should be established.

# fix up the admin user from the command line and turn it into a grid god.
echo "Creating administrative user '$new_admin_name'..."
bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$new_admin_name" "$new_password" "$new_group" "$HOMES_LOC" "$(dirname "$new_admin_name")"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user '$new_admin_name'."
  exit 1
fi

# give the admin back his rights to his own self.
grid_chk chmod "$new_admin_name" +rwx "$new_admin_name"

##############

# become the user from the command line, leaving things ready to test or navigate.
grid_chk logout --all
grid_chk login --username="$(basename $new_admin_name)" --password="$new_password"

echo "Creating test user 'test1'..."
bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERS_LOC/test1" "password1" "$new_group" "$HOMES_LOC" "$USERS_LOC"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user 'test1'."
  exit 1
fi
echo "Creating test user 'test2'..."
bash $XSEDE_TEST_ROOT/library/create-user-and-group.sh "$USERS_LOC/test2" "password2" "$new_group" "$HOMES_LOC" "$USERS_LOC"
if [ $? -ne 0 ]; then
  echo "Failure in setting up user 'test2'."
  exit 1
fi

##############

echo Final log state for mainline single grid setup...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

##############

# we would have bailed if there were an error during the bootstrap process.
echo "Successful bootstrap of grid for '$primary_hostname'."
echo "[$(date)]"
exit 0

