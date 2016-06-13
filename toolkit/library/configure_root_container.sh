#!/bin/bash

# bootstraps a small grid with no connectivity to other grids and sets up an
# administrative account that should have god-like powers over the grid.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"
export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../prepare_tools.sh ../prepare_tools.sh 
fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# fix for windows testing to work properly.
WORKDIR="$(unix_to_dos_path "$WORKDIR")"

#echo WORKDIR is now $WORKDIR

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
echo "Stopping all gffs containers running on this account."
bash "$GFFS_TOOLKIT_ROOT/library/zap_genesis_javas.sh"

# make sure the deployment folder gets created if it doesn't exist...

if [ "$DEPLOYMENT_NAME" == "default" ]; then
  # we don't allow the default deployment to be scrambled up for a bootstrapped grid any more.
  echo -e "\
The bootstrapped grid does not support squishing everything into the 'default'\n\
deployment folder.  Please set a different folder in the DEPLOYMENT_NAME\n\
variable in your config file here:\n\
    '$GFFS_TOOLKIT_CONFIG_FILE'\n\
"
  exit 1
fi

# check out the deployment folder we're about to destroy.
if [ -d "$ACTUAL_DEPLOYMENT_FOLDER" ]; then
  echo "Whacking the deployment folder so we can recreate it."
  rm -rf "$ACTUAL_DEPLOYMENT_FOLDER" 
  check_if_failed "removing deployment folder for new grid"
fi
if [ -d "$ACTUAL_DEPLOYMENT_FOLDER" ]; then
  echo "Bizarre error has occurred and the deployment folder still exists!"
  exit 1
fi

# set up a copy of the deployment generator tool and plug in the necessary
# configuration files.
DEPGEN_LOC="$TEST_TEMP/depgen_tool"
\rm -rf "$DEPGEN_LOC"
check_if_failed "cleaning deployment generator temporary location at $DEPGEN_LOC"
cp -r "$GFFS_TOOLKIT_ROOT/tools/deployment_generator" "$DEPGEN_LOC"
check_if_failed "copying deployment generator archetype to temp location $DEPGEN_LOC"
cp "$DEPGEN_LOC/certificate-config.example" "$DEPGEN_LOC/certificate-config.txt"
check_if_failed "copying certificate template file to real location"
cp "$DEPGEN_LOC/passwords.example" "$DEPGEN_LOC/passwords.txt"
check_if_failed "copying passwords template file to real location"

# fix the passwords file to have the right set.
#hmmm: those are from the file; we need to make those replacements for the ones we know from the grid secrets file.

#hmmm: are any of these done by the fill in bootstrap properties?
#CA_PASSWORD='CApassword'
replace_phrase_in_file "$DEPGEN_LOC/passwords.txt" "CApassword" "$CA_PASSWORD"
check_if_failed "replacing CA password default with real one"
#ADMIN_PASSWORD='ADMpassword'
replace_phrase_in_file "$DEPGEN_LOC/passwords.txt" "ADMpassword" "$ADMIN_PASSWORD"
check_if_failed "replacing admin password default with real one"
#ADMIN_ALIAS='admin'
replace_phrase_in_file "$DEPGEN_LOC/passwords.txt" "ADMIN_ALIAS=.*" "ADMIN_ALIAS=$ADMIN_ALIAS"
check_if_failed "replacing admin alias default with real one"
#TLS_IDENTITY_PASSWORD='container'
replace_phrase_in_file "$DEPGEN_LOC/passwords.txt" "TLS_IDENTITY_PASSWORD=.*" "TLS_IDENTITY_PASSWORD=$TLS_IDENTITY_PASSWORD"
check_if_failed "replacing tls password default with real one"

# toss out the old state directory and any log files at the top.
echo "Cleaning out user directory '$GENII_USER_DIR'"
\rm -rf "$GENII_USER_DIR" "$GFFS_TOOLKIT_ROOT/"*.log "$GFFS_TOOLKIT_ROOT/"*.log.*

# set up the namespace definitions.
#echo Copying namespace properties into place.
#cp "$DEPLOYMENTS_ROOT/default/configuration/template-namespace.properties" "$NAMESPACE_FILE"
#hmmm: is this redoing work above?  yes it was, and wrongly.

if [ -z "$FOLDERSPACE" ]; then
  echo "No FOLDERSPACE variable defined; defaulting to 'xsede.org'"
  export FOLDERSPACE="xsede.org"
fi

##############

# we will generate new certs unless told not to.  the default is to always
# regenerate, since we do not want any vulnerability in a simple bootstrap
# grid from using certs that might have just been hanging around or which were
# retrieved from the repository.
#if [ -z "$REUSE_GRID_CERTIFICATES" ]; then
#is this redundant now?  i think it is.
#  create_grid_certificates
#  check_if_failed "creating certificates for test grid"
#fi

##############

# set up the bootstrapping xscript file.
#fill_in_bootstrapping_properties

##############

prepare_container_configuration
#hmmm: hopefully above doesn't redo any work???

# new flag tells the populate method to copy admin cert as the owner.
bash $DEPGEN_LOC/populate-deployment.sh "$DEPLOYMENT_NAME" $PORT $MACHINE_NAME ADDOWNER
check_if_failed "populating deployment for $DEPLOYMENT_NAME folder"

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
give_administrative_privileges "$new_admin_name" "${primary_hostname}" queues beses admin
check_if_failed "granting admin privileges to $new_admin_name"

# then log in as the new admin before doing anything else.  everything else is done as
# that user, since the admin should have been given complete control over all services
# on the container.
grid_chk logout --all
silent_grid login --username="$(basename $new_admin_name)" --password="$new_password"
check_if_failed Logging in as $new_admin_name

##############

# copy up the flag that shows this is *not* a useful, real, secure grid
# and also let everyone read that file.
multi_grid <<eof
  cp "local:$WORKDIR/a_bogus_grid.txt" "/a_bogus_grid_on_${HOSTNAME}_owned_by_${USER}.txt"
  onerror Failed to copy bogus grid file up to root directory.
  chmod /a_bogus_grid* +r --everyone
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

