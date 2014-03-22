#!/bin/bash

# bootstraps a small grid with no connectivity to other grids and sets up an
# administrative account that should have god-like powers over the grid.
#
# Author: Chris Koeritz
# Author: Vanamala Venkataswamy

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd $WORKDIR
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$XSEDE_TEST_SENTINEL" ]; then
  source ../prepare_tests.sh ../prepare_tests.sh 
fi
source $XSEDE_TEST_ROOT/library/establish_environment.sh

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

# constants...

# the number of slots configured on each BES resource.
if [ -z "$QUEUE_SLOTS" ]; then
  QUEUE_SLOTS=4
fi

# these are needed for the certificate generators.
if [ -z "$C" ]; then C=US; fi
if [ -z "$ST" ]; then ST=Virginia; fi
if [ -z "$L" ]; then L=Charlottesville; fi
if [ -z "$O" ]; then O='Testing Grid'; fi
if [ -z "$OU" ]; then OU='Genesis II'; fi

##############

# begin the active install steps.

echo "[$(date)]"

# we take a dim view of containers that are already started, given that this
# script is a no-nonsense single container grid builder; usually if the
# container is already running or there's an existing user directory, it's an
# oversight.
echo "Cleaning out user directory '$GENII_USER_DIR'"
bash $XSEDE_TEST_ROOT/library/zap_genesis_javas.sh
\rm -rf "$GENII_USER_DIR" "$XSEDE_TEST_ROOT/"*.log "$XSEDE_TEST_ROOT/"*.log.*

if [ $NAMESPACE == 'xsede' ]; then
  echo Copying xsede namespace properties into place.
  cp $DEPLOYMENTS_ROOT/default/configuration/xsede-namespace.properties $DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/configuration/namespace.properties
elif [ $NAMESPACE == 'xcg' ]; then
  echo Copying xcg namespace properties into place.
  cp $DEPLOYMENTS_ROOT/default/configuration/xcg-namespace.properties $DEPLOYMENTS_ROOT/$DEPLOYMENT_NAME/configuration/namespace.properties
else
  echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
  exit 1
fi

##############

# we will generate new certs unless told not to.  the default is to always
# regenerate, since we do not want any vulnerability in a simple bootstrap
# grid from using certs that might have just been hanging around or which were
# retrieved from the repository.
if [ -z "$REUSE_GRID_CERTIFICATES" ]; then
  create_grid_certificates
  check_if_failed "creating certificates for test grid"
fi

launch_container_if_not_running "$DEPLOYMENT_NAME"

echo Startup log state...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

bootstrap_grid
check_if_failed "bootstrapping test grid"

echo After grid basic infrastructure created...
check_logs_for_errors
if [ ! -z "$BACKUP_DEPLOYMENT_NAME" ]; then
  check_logs_for_errors "$BACKUP_DEPLOYMENT_NAME"
fi

get_root_privileges
check_if_failed "acquiring root privileges"

##############

# basic creation of the admin user.
create_group "$new_group"
check_if_failed "creating group $new_group"
create_user "$new_admin_name" "$new_password" "$new_group"
check_if_failed "creating user $new_admin_name"

# power up the new admin guy.
give_administrative_privileges "$new_admin_name" "${primary_hostname}" "" "" admin
check_if_failed "granting admin privileges to $new_admin_name"

# then log in as the new admin before doing anything else.  everything else is done as
# that user, since the admin should have been given complete control over all services
# on the container.
grid_chk logout --all
grid login --username="$(basename $new_admin_name)" --password="$new_password"
check_if_failed Logging in as $new_admin_name

##############

# copy up the flag that shows this is *not* a useful, real, secure grid
# and also let everyone read that file.
multi_grid <<eof
  cp local:$WORKDIR/a_bogus_grid.txt /
  onerror Failed copy bogus grid file up to root directory.
  chmod /a_bogus_grid.txt +r --everyone
  onerror Failed to change permissions on bogus grid file.
eof
check_if_failed Setting up bogus grid marker file.

##############

# make a link for the bootstrap container to use the hostname we were given.
if [ "$BOOTSTRAP_LOC" != "$CONTAINERS_LOC/$primary_hostname" ]; then
  echo "Adding container alias '$primary_hostname' for bootstrap..."
  grid_chk ln $BOOTSTRAP_LOC "$CONTAINERS_LOC/$primary_hostname"
  check_if_failed "linking in hostname alias for bootstrap container"
fi

##############

# create a queue for the grid and create a BES that the queue can use for job submission.

create_queue "$QUEUE_PATH" "$CONTAINERS_LOC/$primary_hostname" "$new_admin_name" "$new_group"
check_if_failed "creating queue $QUEUE_PATH"
create_BES "$CONTAINERS_LOC/$primary_hostname" "$new_admin_name" "$QUEUE_PATH" "$new_group"
check_if_failed "creating BES for $CONTAINERS_LOC/$primary_hostname"
give_queue_a_resource "$QUEUE_PATH" "$BES_CONTAINERS_LOC/${primary_hostname}-bes" $QUEUE_SLOTS
check_if_failed "giving queue a BES resource"

##############

# power up admin again, now that there are some assets to administrate.
give_administrative_privileges "$new_admin_name" "${primary_hostname}" queues beses
check_if_failed "granting admin privileges after more infrastructure established"

##############

# by here, the basic infrastructure of the grid itself should be established.

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

