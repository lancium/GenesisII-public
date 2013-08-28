#!/bin/bash

# powerful management functions for the grid or containers.
#
# Author: Chris Koeritz

##############

# define the cert-tool appropriately for the platform.

CERTO=$GENII_INSTALL_DIR/cert-tool
if [ "$OS" == "Windows_NT" ]; then
  CERTO="cmd /c $(echo $GENII_INSTALL_DIR | tr / \\\\)\\cert-tool.bat"
fi

##############

# the number of lines we scan for the container startup phrase.
export LINE_COUNT_FOR_START_CHECK=200

##############

# general functions that affect client and container.

# this takes one argument, which is a new directory to use for the
# user state.  this must always be followed by a restore_userdir without
# any intervening new call to save_and_switch.
function save_and_switch_userdir()
{
  local new_dir="$1"; shift
  HOLD_USERDIR="$GENII_USER_DIR"
  export GENII_USER_DIR="$new_dir"
}

# restores the previous user directory.  handles one level of rollback.
function restore_userdir()
{
  export GENII_USER_DIR="$HOLD_USERDIR"
}

##############

# container related functions.

# starts up the genesis2 container on this machine if it is not found running.
# takes an optional deployment name.  note that it is crucial that you have
# switched to the right user directory if you are trying to launch a backup
# container; see the save_and_switch_userdir method.
function launch_container_if_not_running()
{
  dep="$1"; shift
  # launch the container here if we don't think it's running.
  running="$(find_genesis_javas $dep)"
  if [ ! -z "$running" ]; then
    echo "The container already seems to be running for this user."
    return
  else
    echo "Grid container not seen running--starting it."
  fi
  launch_container "$dep"
}

# returns the standard location for the container log file, or if this
# is the backup deployment, it returns a contriver log file.
function get_container_logfile()
{
  local DEP_NAME="$1"; shift
  local extra=
  if [ -z "$DEP_NAME" ]; then
    DEP_NAME=default
  else
    extra="_${DEP_NAME}"
  fi
  if [ "$DEP_NAME" != "default" ]; then
    # this is kludgy, but we need to separate logging for backup containers...
    # and right now we consider anything we'd launch which is not called
    # "default" to be a backup container.
    echo "$TEST_TEMP/container_$(hostname)_${USER}$extra.log"
    return 0
  fi
  # log file for normal deployment.
  to_return="$(grep log4j.appender.LOGFILE.File $GENII_INSTALL_DIR/lib/genesisII.container.log4j.properties | tr -d '\r\n' | sed -e 's/.*=\(.*\)/\1/' | sed -e "s%\${user.home}%$HOME%")"
  echo "$to_return"
}

# returns the standard location for the client log file.
function get_client_logfile()
{
  to_return="$(grep log4j.appender.LOGFILE.File $GENII_INSTALL_DIR/lib/genesisII.client.log4j.properties | tr -d '\r\n' | sed -e 's/.*=\(.*\)/\1/' | sed -e "s%\${user.home}%$HOME%")"
  echo "$to_return"
}

# just barrels ahead and tries to launch the container.  takes an optional
# deployment name.  note that it is crucial to switch to the right user
# directory if trying to launch a backup container; see the
# save_and_switch_userdir method for details.
function launch_container()
{
  local DEP_NAME="$1"; shift

  # move the log out of the way so we don't get fooled by old startup noise.
  containerlog="$(get_container_logfile "$DEP_NAME")"
#no, this is bad for keeping the log updating.
#  if [ -f "$containerlog" ]; then
#    \mv "$containerlog" "${containerlog}.hold" 
#  fi

  # ensure that we have at least our scanning factor worth of lines in the buffer
  # that are not the restart phrase.
  local d;
  if [ -f "$containerlog" ]; then
    for ((d=0; d < $LINE_COUNT_FOR_START_CHECK; d++)); do
      echo "-" >>"$containerlog"
    done
  fi

  pushd "$GENII_INSTALL_DIR" &>/dev/null

  echo "Launching Genesis II container for deployment $DEP_NAME..."
  CONTAINERLOGFILE="$(get_container_logfile "$DEP_NAME")"
  echo "expecting to write container log at: $CONTAINERLOGFILE"
  redirection=0
  if [ "$DEP_NAME" != "default" ]; then
    # if we're not a default deployment, we need to redirect output to save it.
    redirection=1
  fi
  extra_prefix=
  extra_suffix=
  use_shell=bash
  runner="$GENII_INSTALL_DIR/runContainer.sh"
  if [ ! -f "$runner" ]; then
    runner=$GENII_INSTALL_DIR/XCGContainer
    extra_suffix="start"
  fi
  if [ ! -f "$runner" ]; then
    use_shell=
    runner="$GENII_INSTALL_DIR/wrapper-windows-x86-32.exe"
    extra_suffix="$GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/conf/wrapper.conf"
  fi
  if [ ! -f "$runner" ]; then
    runner="$(echo $GENII_INSTALL_DIR/runContainer.bat | sed -e 's/\//\\\\/g')"
    use_shell=cmd
    extra_suffix=
    if [ -f "$runner" ]; then
      extra_prefix="/c"
    fi
  fi
  if [ ! -f "$runner" ]; then
    echo "Failed to find the launcher for Genesis II."
    exit 1
  fi

#echo "shell=$use_shell extra_prefix=$extra_prefix runner=$runner extra_suffix=$extra_suffix"
  if [ $redirection -eq 0 ]; then
    $use_shell $extra_prefix $runner $extra_suffix $DEP_NAME &>/dev/null &
  else
    $use_shell $extra_prefix $runner $extra_suffix $DEP_NAME &>$CONTAINERLOGFILE &
  fi

  # snooze to allow the container to get going.  the counter measures number of 10 second
  # sleeps to allow.
  i=20
  while [ -z "$(if [ -f $CONTAINERLOGFILE ]; then tail -n $LINE_COUNT_FOR_START_CHECK < $CONTAINERLOGFILE | grep "Restarting all BES Managers"; fi)" ]; do
    if [ $i -le 0 ]; then
      echo "Failed to find proper phrasing in container log to indicate it started; continuing anyway."
      break;
    fi
    echo "Pausing to await container start..."
    sleep 10
    i=$(expr $i - 1)
  done
  echo "Container has started."

  # now move the old log back, since the other was not the interesting one from bootstrapping.
#  if [ -f "${containerlog}.hold" ]; then
#    if [ -f "$containerlog" ]; then
#      # scavenge the startup noises so we have a complete record.
#      cat "$containerlog" >>"${containerlog}.hold"
#    fi
#    \mv "${containerlog}.hold" "$containerlog" 
#  fi

  popd &>/dev/null
}

# shuts down the container if we had previously started it ourselves.
# only supports shutting it down if the above launch container function was used.
function stop_container()
{
return;  #disabled currently.
  # only stop the grid container if we had started it here.
  if [ "$running" == "" ]; then
    # if we set up a container in the background, it's time to shut it down.
#hmmm: no longer setting this waiting flag.  need another tracking method.
#      was not a great idea in the first place.
    if [ $waiting -eq 1 ]; then
      kill %1
      wait %1
    else
      # we can tell the launcher command to deal with this.
      $runner stop
    fi
  fi
}

# this saves the grid deployments and user data for the normal container and
# the backup, if enabled.
function save_grid_data()
{
  local data_zip="$1"; shift
  if [ -z "$data_zip" ]; then
    echo "This function requires a zip file name for storing the grid data."
    return 1
  fi
  \rm -f "$data_zip"
  # now grab up a copy of the normal state directory.
  # we need to zip it without a full path, so we can easily unzip it to the right place.
  pushd $GENII_USER_DIR/.. &>/dev/null
  zip -r $HOME/bootstrap_save.zip "$(basename "$GENII_USER_DIR")" &>/dev/null
  popd &>/dev/null
  # zip the backup state too, if there is one.
  if [ ! -z "$BACKUP_USER_DIR" ]; then
    pushd $BACKUP_USER_DIR/.. &>/dev/null
    zip -r $HOME/bootstrap_save.zip "$(basename "$BACKUP_USER_DIR")" &>/dev/null
    popd &>/dev/null
  fi
  # and backup the deployment info.
  pushd "$GENII_INSTALL_DIR/deployments/.." &>/dev/null
  zip -r $HOME/bootstrap_save.zip deployments &>/dev/null
  popd &>/dev/null
  \mv $HOME/bootstrap_save.zip "$data_zip"
}

##############

# certificate methods.

# requires three parameters, the pfx file to create, the password for it, and the
# alias to use for the certificate inside the pfx.
function create_bootstrap_signing_certificate()
{
  local CA_PFX="$1"; shift
  local CA_PASSWORD="$1"; shift
  local CA_ALIAS="$1"; shift

  local UBER_CA_PFX="$CA_PFX-base.pfx"
  local UBER_CA_ALIAS="base-key"
  $CERTO gen -dn="C=US, ST=Virginia, L=Charlottesville, O=GENIITEST, OU=Genesis II, CN=skynet" -output-storetype=PKCS12 "-output-entry-pass=$CA_PASSWORD" -output-keystore=$UBER_CA_PFX "-output-keystore-pass=$CA_PASSWORD" "-output-alias=$UBER_CA_ALIAS" -keysize=2048
  check_if_failed "generating base of CA keypair"

  # and create the base key's certificate file.
#  local cert_file="$(echo $UBER_CA_PFX | sed -e 's/\.pfx$/\.cer/')"
  local cert_file="$(dirname $UBER_CA_PFX)/$(basename $UBER_CA_PFX ".pfx").cer"
  $JAVA_HOME/bin/keytool -export -file "$cert_file" -keystore "$UBER_CA_PFX" -storepass "$CA_PASSWORD" -alias "$UBER_CA_ALIAS" -storetype "PKCS12"
  check_if_failed "generating certificate file $cert_file for $UBER_CA_PFX"

  # now create the real signing certificate, with full CA apparel.
  create_certificate_using_CA "$UBER_CA_PFX" "$CA_PASSWORD" "$UBER_CA_ALIAS" "$CA_PFX" "$CA_PASSWORD" "$CA_ALIAS" "skynet"
  check_if_failed "generating signing keypair"

  # figure out where we are storing the files.
#  local thedir="$(dirname $CA_PFX)"
  # compute the certificate filename from the base of the keypair file.
#  local CA_CER="$(basename $CA_PFX ".pfx").cer"
}

# folds all the certificates into a trusted.pfx file.  needs a directory location.
function create_bootstrap_trusted_pfx()
{
  local dirname="$1"; shift
  for certfile in $dirname/*.cer; do
    echo -e "\nAdding certificate '$certfile' to trust store."
    local output_alias=$(basename "$certfile" .cer)
    echo -e "\tusing alias $output_alias"
    $CERTO import -output-keystore="$dirname/trusted.pfx" -output-keystore-pass=trusted -base64-cert-file="$certfile" -output-alias="$output_alias"
    check_if_failed "adding certificate for $certfile"
  done
}

# creates a new certificate based on an existing CA in pkcs12 format.
# 1) pfx file to use as CA, 2) password for CA pfx, 3) alias for the ca key-pair,
# 4) pfx file to generate, 5) password for new pfx, 6) alias for new key-pair,
# 7) common name entry for new key-pair (CN).
function create_certificate_using_CA()
{
  if [ $# -lt 7 ]; then
    echo "create_certificate_using_CA needs 7 parameters: the pfx file with the CA keypair,"
    echo "the password for that file, the alias for the CA pfx, the new pfx file"
    echo "to generate using the CA, the password for the new pfx containing the new"
    echo "keypair, the alias for the new pfx, and the CN (common name) entry for the"
    echo "new key-pair."
    return 1
  fi
  local THE_CA_PFX="$1"; shift
  local THE_CA_PASS="$1"; shift
  local THE_CA_ALIAS="$1"; shift
  local NEW_PFX="$1"; shift
  local NEW_PASS="$1"; shift
  local NEW_ALIAS="$1"; shift
  local CN_GIVEN="$1"; shift

  echo create cert $NEW_PFX with alias $NEW_ALIAS and CN=$CN_GIVEN

  # first generate the private and public key into the pkcs12 archive.
  $CERTO gen "-dn=C=$C, ST=$ST, L=$L, O=$O, OU=$OU, CN=$CN_GIVEN" -output-storetype=PKCS12 "-output-entry-pass=$NEW_PASS" "-output-keystore=$NEW_PFX" "-output-keystore-pass=$NEW_PASS" "-output-alias=$NEW_ALIAS" "-input-keystore=$THE_CA_PFX" "-input-keystore-pass=$THE_CA_PASS" -input-storetype=PKCS12 "-input-entry-pass=$THE_CA_PASS" -input-alias="$THE_CA_ALIAS" -keysize=2048
  check_if_failed "generating $NEW_PFX from $THE_CA_PFX"
  # and create its certificate file.
#  local cert_file="$(echo $NEW_PFX | sed -e 's/\.pfx/\.cer/')"
  local cert_file="$(dirname $NEW_PFX)/$(basename $NEW_PFX ".pfx").cer"
  $JAVA_HOME/bin/keytool -export -file "$cert_file" -keystore "$NEW_PFX" -storepass "$NEW_PASS" -alias "$NEW_ALIAS" -storetype "PKCS12"
  check_if_failed "generating certificate file $cert_file for $NEW_PFX"
}

##############

# grid-wide operations.

function create_grid_certificates()
{
  pushd $GENII_INSTALL_DIR &>/dev/null

  local SECURITY_DIR="deployments/$DEPLOYMENT_NAME/security"

  local SIGNING_PFX="$SECURITY_DIR/signing-cert.pfx"
  local SIGNING_ALIAS="signing-cert"
  local SIGNING_PASSWD="signer"

  local ADMIN_PFX="$SECURITY_DIR/admin.pfx"
  local ADMIN_CER="$SECURITY_DIR/admin.cer"
  local ADMIN_PASSWD="keys"

  # clean up any existing certificates.
  \rm -f $SECURITY_DIR/*.pfx $SECURITY_DIR/*.cer

  # fix the patch certificate, which doesn't exist yet, and the app-url.
  sed -i -e 's/^\(edu.virginia.vcgr.appwatcher.patch-signer-certificate.0=.*\)/#\1/' -e 's/^\(edu.virginia.vcgr.appwatcher.application-url.0=.*\)/#\1/' ApplicationWatcher/genii-base-application.properties
  check_if_failed "patching ApplicationWatcher properties for bootstrap"

  # create the root signing certificate.  should be an actual CA now.
  create_bootstrap_signing_certificate "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS"
  check_if_failed "creating a signing certificate for bootstrap"

  # create the admin certificate.
  create_certificate_using_CA "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS" $ADMIN_PFX "$ADMIN_PASSWD" skynet "skynet admin"
  check_if_failed "creating skynet admin certificate using CA"
  cp "$ADMIN_CER" "$SECURITY_DIR/default-owners/admin.cer"
  check_if_failed "copying admin certificate into default-owners"

####
#hmmm: toss this.
  create_certificate_using_CA "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS" $SECURITY_DIR/secrun.pfx sec secure-runner "Secure Runnable Cert"
  check_if_failed "creating secrun certificate using CA"
####

  create_certificate_using_CA "$SIGNING_PFX" "$SIGNING_PASSWD" "$SIGNING_ALIAS" $SECURITY_DIR/tls-cert.pfx tilly tls-cert "TLS certificate"
  check_if_failed "creating TLS certificate using CA"

  create_bootstrap_trusted_pfx $SECURITY_DIR

  popd &>/dev/null
}

# runs the bootstrap XML script to establish our basic grid configuration.
function bootstrap_grid()
{
  # go to the folder for these steps due to some squirreliness.
  pushd $GENII_INSTALL_DIR &>/dev/null

  # perform the basic setup of the grid, already canned for us.
  echo "Bootstrapping default grid configuration..."

  # fix up the server config with our hostname.
#  replace_phrase_in_file "$DEP_DIR/configuration/server-config.xml" "\(name=.edu.virginia.vcgr.genii.container.external-hostname-override. value=\"\)[^\"]*\"" "\1localhost\""

  bootstrap_file="deployments/$DEPLOYMENT_NAME/configuration/bootstrap.xml"
  if [ $NAMESPACE == 'xsede' ]; then
    echo -e "\n---- Choosing 'xsede' namespace for bootstrap ----"
    cp "deployments/$DEPLOYMENT_NAME/configuration/xsede-bootstrap.xml" $bootstrap_file
  elif [ $NAMESPACE == 'xcg' ]; then
    echo -e "\n---- Choosing 'xcg' namespace for bootstrap ----"
    cp "deployments/$DEPLOYMENT_NAME/configuration/xcg-bootstrap.xml" $bootstrap_file
  else
    echo "Unknown namespace type--the NAMESPACE variable is unset or unknown"
    exit 1
  fi
  grid_chk script "local:${bootstrap_file}"

  # jump back out of the install directory.  the deployment behaves
  # oddly if we aren't in there, but nothing else should require being in the install dir.
  popd &>/dev/null
}

# these are the steps that make a user or group (rns path) into a real powerhouse on the grid.
# this requires that the user calling this be logged in as an administrator on the container
# specified, as well as being a grid administrator.
function give_administrative_privileges()
{
  local rnspath="$1"; shift
  local container="$1"; shift
  if [ -z "$rnspath" -o -z "$container" ]; then
    echo "Error in give_administrative_privileges function--need to specify user path and"
    echo "container name."
    exit 1
  fi
  if [ "$(basename $container)" == "$container" ]; then
    # they gave us the raw container name, so assume they're talking about the default
    # location for a container.
    container="$CONTAINERS_LOC/$container"
  fi
  echo "Giving '$rnspath' administrative rights across '$container'..."
  # this set of commands makes the rns path into an admin, mainly on the container.
  # the bes for the container is assumed to be created by us, so one may have to add
  # perms manually if it is not in "$BES_CONTAINERS_LOC/{shortContainerName}-bes".
#hmmm: this should use the xsede admin script to do the perms on services!
  multi_grid &>/dev/null <<eof
    chmod "$rnspath" +rwx "$rnspath"
    onerror chmod for rnspath failed.
    chmod "/" +rwx $rnspath
    onerror chmod for / failed.
    chmod "$CONTAINERS_LOC" +rwx $rnspath
    chmod "$container" +rwx $rnspath
    chmod "$container/Services/X509AuthnPortType" +rwx $rnspath
    chmod "$container/Services/EnhancedRNSPortType" +rwx $rnspath
    chmod "$container/Services/GeniiBESPortType" +rwx $rnspath
    chmod "$container/Services/QueuePortType" +rwx $rnspath
    chmod "$container/Services/LightWeightExportPortType" +rwx $rnspath
    chmod "$container/Services/RandomByteIOPortType" +rwx $rnspath
    chmod "$BES_CONTAINERS_LOC" +rwx $rnspath
    chmod "$BES_CONTAINERS_LOC/$(basename $container)-bes" +rwx $rnspath
    chmod "$USERS_LOC" +rwx $rnspath
    chmod "$GROUPS_LOC" +rwx $rnspath
    chmod "$HOMES_LOC" +rwx $rnspath
    chmod "$QUEUES_LOC" +rwx $rnspath
eof
  if [ $? -ne 0 ]; then
    echo "Administrative steps failed for $rnspath"
    exit 1
  fi

#  echo Output from granting admin privileges:
#  cat $GRID_OUTPUT_FILE
}

##############

# scripts for managing queues, BESes, resources, etc.

# sets up a BES on a container, where the full path to the container is expected (e.g.
# "/containers/busby" rather than just "busby").  the second parm should be the number
# of instances of the BES to create.
function create_BES()
{
  local container_name="$1"; shift
  local owner_name="$1"; shift
  local queue_name="$1"; shift
  local group_name="$1"; shift
  local bes_name="$1"; shift  # optional

  local short_container="$(basename $container_name)"

  if [ -z "$bes_name" ]; then
    bes_name="$BES_CONTAINERS_LOC/${short_container}-bes"
  fi

  if [ -z "$container_name" -o -z "$owner_name" -o -z "$queue_name" -o -z "$group_name" ]; then
    echo "create_BES function needs a container name to operate on, a user to give access to the"
    echo "container, a queue to also give access, and a group to also give access."
    exit 1
  fi

  echo "Creating a BES on the container '$container_name'..."
  grid_chk create-resource "$container_name/Services/GeniiBESPortType" "$bes_name"

  echo "Giving user '$owner_name' owner rights to the BES..."
  grid_chk chmod "$bes_name" +rwx "$owner_name"

  echo "Giving queue '$queue_name' normal rights to the BES..."
  grid_chk chmod "$bes_name" +rx "$queue_name"

  echo "Giving queue '$group_name' normal rights to the BES..."
  grid_chk chmod "$bes_name" +rx "$group_name"
}

# establishes a queue given the full path to a container name.
function create_queue()
{
  local queue_path="$1"; shift
  local container_name="$1"; shift
  local owner_name="$1"; shift
  local group_name="$1"; shift

#  local short_container="$(basename $container_name)"
  if [ -z "$queue_path" -o -z "$container_name" -o -z "$owner_name" -o -z "$group_name" ]; then
    echo "create_queue function needs a path to put the queue, the container"
    echo "name to operate on, the owner name for the queue, and a user name"
    echo "to give access to the queue."
    exit 1
  fi
  echo "Creating queue resource on container '$container_name'..."
  grid_chk create-resource "$container_name/Services/QueuePortType" "$queue_path"

  echo "Giving user '$owner_name' owner rights to the queue..."
  grid_chk chmod "$queue_path" +rwx "$owner_name"

  echo "Giving group '$group_name' normal rights to the queue..."
  grid_chk chmod "$queue_path" +rx "$group_name"
}

# takes a BES resource and links it under the queue in question.
# the resource is given the specified number of slots for processing jobs.
function give_queue_a_resource()
{
  local queue_name="$1"; shift
  local bes_name="$1"; shift
  local queue_slots="$1"; shift
  if [ -z "$bes_name" -o -z "$queue_name" -o -z "$queue_slots" ]; then
    echo "give_queue_a_resource function needs queue name, bes resource name, and slot count."
    exit 1
  fi
  local short_bes="$(basename $bes_name)"
  echo "Making BES '$bes_name' available on the queue '$queue_name'..."
  grid_chk ln "$bes_name" "$queue_name/resources/$short_bes"
  echo "Setting queue slots to ${queue_slots}..."
  grid_chk qconfigure "$queue_name" "$short_bes" "$queue_slots"
}

##############

# borrowed from the http://feistymeow.org codebase.
  # locates a process given a search pattern to match in the process list.
  function psfind() {
    local PID_DUMP="$(mktemp "$TMP/zz_pidlist.XXXXXX")"
    local PIDS_SOUGHT=()
    local patterns=($*)
    if [ "$OS" == "Windows_NT" ]; then
      # needs to be a windows format filename for 'type' to work.
      if [ ! -d c:/tmp ]; then
        mkdir c:/tmp
      fi
      # windows7 magical mystery tour lets us create a file c:\\tmp_pids.txt, but then it's not really there
      # in the root of drive c: when we look for it later.  hoping to fix that problem by using a subdir, which
      # also might be magical thinking from windows perspective.
      tmppid=c:\\tmp\\pids.txt
      # we have abandoned all hope of relying on ps on windows.  instead
      # we use wmic to get full command lines for processes.
      # this does not exist on windows home edition.  we are hosed if that's
      # what they insist on testing on.
      wmic /locale:ms_409 PROCESS get processid,commandline </dev/null >"$tmppid"
      local flag='/c'
      if [ ! -z "$(uname -a | grep "^MING" )" ]; then
        flag='//c'
      fi
      # we 'type' the file to get rid of the unicode result from wmic.
      cmd $flag type "$tmppid" >$PID_DUMP
      \rm "$tmppid"
      local CR=''  # embedded carriage return.
      local appropriate_pattern="s/^.*  *\([0-9][0-9]*\)[ $CR]*\$/\1/p"
      for i in "${patterns[@]}"; do
        PIDS_SOUGHT+=$(cat $PID_DUMP \
          | grep -i "$i" \
          | sed -n -e "$appropriate_pattern")
        if [ ${#PIDS_SOUGHT[*]} -ne 0 ]; then
          # we want to bail as soon as we get matches, because on the same
          # platform, the same set of patterns should work to find all
          # occurrences of the genesis java.
          break;
        fi
      done
    else
      /bin/ps $extra_flags wux >$PID_DUMP
      # pattern to use for peeling off the process numbers.
      local appropriate_pattern='s/^[-a-zA-Z_0-9][-a-zA-Z_0-9]*  *\([0-9][0-9]*\).*$/\1/p'
      # remove the first line of the file, search for the pattern the
      # user wants to find, and just pluck the process ids out of the
      # results.
      for i in "${patterns[@]}"; do
        PIDS_SOUGHT=$(cat $PID_DUMP \
          | sed -e '1d' \
          | grep -i "$i" \
          | sed -n -e "$appropriate_pattern")
        if [ ${#PIDS_SOUGHT[*]} -ne 0 ]; then
          # we want to bail as soon as we get matches, because on the same
          # platform, the same set of patterns should work to find all
          # occurrences of the genesis java.
          break;
        fi
      done
    fi
    if [ ! -z "$PIDS_SOUGHT" ]; then echo "$PIDS_SOUGHT"; fi
    /bin/rm $PID_DUMP
  }

##############

declare -a genesis_java_pids=()

# locates any running processes that seem to be from genesis 2.
function find_genesis_javas()
{
  pattern="$1"; shift
  # now a cascade of attempts to find some processes.
  user=$USER
  if [ "$OS" == "Windows_NT" ]; then
    unset user
  fi
  temp_array=()
  # make sure we match the user-defined pattern also.
  addon=".*$pattern"
  local patterns=""
  # build a list of the true patterns we want.
  for i in "java.*genesis" "java.*ApplicationWatcher" "JavaServiceWrapper" "JavaSe.*wrapper.*windows" "Genesi.*java" "ApplicationWatcher"; do
    patterns+="${i}${addon} "
  done
  # find all the processes matching those patterns.
  genesis_java_pids="$(psfind $patterns)"
}

##############

# helper method that checks for a phrase in a file and reports the number of occurrences.
function show_count()
{
  local phrase="$1"; shift
  local file="$1"; shift
  echo -ne "  $phrase\t"
  grep -i "$phrase" "$file" | wc -l
}

# makes a report of successes and failures found in the container and client logs.
function check_logs_for_errors()
{
  local DEP_NAME="$1"; shift
  if [ -z "$DEP_NAME" ]; then
    DEP_NAME=default
  fi
  echo "Looking for issues and actions in the logs..."
  file="$(get_container_logfile "$DEP_NAME")"
  echo "File: $file..."
  for i in grant fail warn error; do
    show_count "$i" "$file"
  done
  # only print client stats for the main container.
  if [ "$DEP_NAME" == "default" ]; then
    file="$(get_client_logfile)"
    echo "File: $file..."
    for i in fail warn error; do
      show_count "$i" "$file"
    done
  fi
}

##############

###temporary method; only needed for activity 123 grid, which has a different port types list.

# this substitutes the activity 123 port type list into the gffs structure
# jar.  this is necessary since this grid was built without the jndi port
# enabled in port types, which affects every EPR.  xcg3 and other grids
# should never need the modified port types list.
function replace_activity123_porttypes()
{
  pushd $GENII_INSTALL_DIR
  jarlist=($(find $GENII_INSTALL_DIR -iname "gffs-structure.jar"))
#echo jarlist is: ${jarlist[@]}
  for jarfile in ${jarlist[@]}; do
    # make a temporary directory for our work.
    TMPACTD=tempDir
    \rm -rf $TMPACTD
    mkdir -p $TMPACTD/config
    cp subprojects/gffs-structure/trunk/config/activity123-known-porttypes.xml $TMPACTD/config/known-porttypes.xml
    cd $TMPACTD
    echo "updating port types in jar file: $jarfile"
    jar uf $jarfile config/known-porttypes.xml
    cd ..
    # now that we're done, clean up.
    \rm -rf $TMPACTD
  done
  popd
}

##############

