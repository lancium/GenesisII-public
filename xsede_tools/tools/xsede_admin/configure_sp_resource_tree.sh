#!/bin/bash

##############
#
# Author: Vanamala Venkataswamy
# Author: Chris Koeritz (final changes for activity 126)
#
# This script can be used to:
# 1. created SP resources in xsede namespace (by gffs-admins)
# 2. link a genesis2 container to xsede namespace (by SP admins)
# 3. link a unicore bes to xsede namespace (by SP admins)
# 4. create a queue and link any type of bes into it (by SP admins)
#
##############

function add_resolver_to_item()
{
  resolver_parm_line=$1; shift
  replicate_parm_line=$1; shift
  item=$1; shift

#hmmm: hide output from complaining failures where resolver already exists?
  
  "$GENII_INSTALL_DIR/grid" resolver $resolver_parm_line $item /etc/resolvers/rootResolver 
  # if it already had a resolver or we couldn't create one, we don't want
  # to replicate it.
  if [ $? -eq 0 ]; then
    "$GENII_INSTALL_DIR/grid" replicate $replicate_parm_line $item /resources/xsede.org/containers/gffs-2.xsede.org
  fi
}

# this expects the rootResolver and the root replica container (gffs-2) to already exist.
function replicate_resource_hierarchy()
{
  top_lev_file=$(mktemp /tmp/toplev.XXXXXX)

  "$GENII_INSTALL_DIR/grid" ls $service_provider_path \
      | tail -n +2 >"$top_lev_file"

  echo "[$service_provider_path]"
  add_resolver_to_item "" "" $service_provider_path

  while read top_level_item; do
    if [ ! -z "$top_level_item" ]; then
      mid_lev_file=$(mktemp /tmp/midlev.XXXXXX)
      outer_item="$service_provider_path/$top_level_item"
      echo "  [$outer_item]"
      "$GENII_INSTALL_DIR/grid" ls $outer_item \
          | tail -n +2 >"$mid_lev_file"
      add_resolver_to_item "" "" $outer_item
      while read mid_level_item; do
        if [ ! -z "$mid_level_item" ]; then
          inner_item="$outer_item/$mid_level_item"
          echo "    [$inner_item]"
          add_resolver_to_item "" "" $inner_item
        fi
      done <"$mid_lev_file"
      \rm "$mid_lev_file"
    fi
  done <"$top_lev_file"
  \rm "$top_lev_file"
}

function chmod_for_all()
{
  path="$1"; shift
  errchk="$(mktemp /tmp/errchk.XXXXXX)"
  "$GENII_INSTALL_DIR/grid" <<eof &>"$errchk"
    chmod $path +rwx $grid_identity
    onerror failed to give rights on $path to $grid_identity
    chmod $path +rwx /groups/xsede.org/gffs-admins
    onerror failed to give rights on $path to gffs-admins
    chmod $path +r /groups/xsede.org/gffs-users
    onerror failed to give rights on $path to gffs-users
eof
  if [ $? -ne 0 ]; then
    grep OnError "$errchk"
    echo -e "****\nUnexpected Failure in setting up permissions!\n****"
  fi
  rm "$errchk"
}

function create_resources_structure()
{
  echo "Creating $service_provider_path"
  "$GENII_INSTALL_DIR/grid" mkdir -p $service_provider_path
  if [ $? -ne 0 ]; then
    echo "Failed to create top-level resource path: $service_provider_path"
    exit 1
  fi

  chmod_for_all $service_provider_path

  echo "Creating $service_provider_path/containers"
  "$GENII_INSTALL_DIR/grid" mkdir -p $service_provider_path/containers
  chmod_for_all $service_provider_path/containers

  echo "Creating $service_provider_path/storage-containers"
  "$GENII_INSTALL_DIR/grid" mkdir -p $service_provider_path/storage-containers
  chmod_for_all $service_provider_path/storage-containers

  echo "Creating $service_provider_path/bes-containers"
  "$GENII_INSTALL_DIR/grid" mkdir -p $service_provider_path/bes-containers
  chmod_for_all $service_provider_path/bes-containers

  echo "Creating $service_provider_path/queues"
  "$GENII_INSTALL_DIR/grid" mkdir -p $service_provider_path/queues
  chmod_for_all $service_provider_path/queues
}

function link_sp_container()
{
  echo "Linking new container $service_provider_path/containers/$container_name"
  "$GENII_INSTALL_DIR/grid" ln --service-url=$g2_url $service_provider_path/containers/$container_name
  "$GENII_INSTALL_DIR/grid" ping $service_provider_path/containers/$container_name
  if [ $? != 0 ];then
    echo "Container could not be linked successfully"
  else
    echo "Successfully linked $service_provider_path/containers/$container_name"
  fi

  echo "Setting permissions for $grid_identity on $service_provider_path/containers/$container_name"
  chmod_for_all $service_provider_path/containers/$container_name
  "$GENII_INSTALL_DIR/grid" script local:./configure_container_permissions.xml $service_provider_path/containers/$container_name $grid_identity
}

function create_unicore_bes()
{
  url_for_cert=`echo $u6_url|awk -F/ '{print $3}'` > /dev/null
  echo "Trying to download certificate from $url_for_cert"
  which openssl > /dev/null
  certificate_path=""
  if [ $? == 0 ];then
    echo -n|openssl s_client -connect $url_for_cert | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/$url_for_cert.cer
    if [ -s /tmp/$url_for_cert.cer ];then
      echo "Got certificate /tmp/$url_for_cert.cer "
      certificate_path=/tmp/$url_for_cert.cer
    else
      echo "Problem downloading certificate; please enter local path to unicore host certificate"
      read certificate_path
    fi
  else
    echo "Unable to find openssl; please enter local path to unicore host certificate"
    read certificate_path
  fi

  echo "Linking new unicore bes $service_provider_path/bes-containers/$bes_name"
  "$GENII_INSTALL_DIR/grid" mint-epr --link=$service_provider_path/bes-containers/$bes_name --certificate-chain=local:$certificate_path $u6_url
}

function create_queue_for_bes()
{
  echo "Creating $service_provider_path/queues/$bes_name-queue queue resource" 
  "$GENII_INSTALL_DIR/grid" create-resource $service_provider_path/containers/$container_name/Services/QueuePortType $service_provider_path/queues/$bes_name-queue
  chmod_for_all $service_provider_path/queues/$bes_name-queue

  echo "Linking $service_provider_path/bes-containers/$bes_name into $service_provider_path/queues/$bes_name-queue/resources/$bes_name "
  "$GENII_INSTALL_DIR/grid" ln $service_provider_path/bes-containers/$bes_name $service_provider_path/queues/$bes_name-queue/resources/$bes_name

  echo "Setting rx permissions on $service_provider_path/queues/$bes_name-queue for $grid_identity"
  chmod_for_all $service_provider_path/queues/$bes_name-queue

  echo "Setting queue slots to $num_slots for $bes_name-queue"
  "$GENII_INSTALL_DIR/grid" qconfigure $service_provider_path/queues/$bes_name-queue $bes_name $num_slots
}

##############

function print_instructions()
{
  scrip=$(basename $0)
  echo "Usage:"
  echo
  echo "Create the GFFS folders for a Service Provider resource tree;"
  echo "should only be run by members of the gffs-admins group:"
  echo
  echo "  $scrip -t -i grid-identity -n resource-name"
  echo
  echo "Link a container into a Service Provider resource tree;"
  echo "can be run by the SP admin group:"
  echo
  echo "  $scrip -g -i grid-identity \\"
  echo "    -h hostname:port -n resource-name -c container-name"
  echo
  echo "Link a Unicore BES into a Service Provider resource hierarchy:"
  echo "can be run by the SP admin group."
  echo
  echo "  $scrip -u -i grid-identity \\"
  echo "     -h hostname:port/SITEID -n resource-name -b bes-name"
  echo
  echo "Create a queue on SP container and link BES into it;"
  echo "can be run by the SP admin group."
  echo
  echo "  $scrip -l -i grid-identity \\"
  echo "     -n resource-name -c container-name -b bes-name -s slot-count"
  echo
  echo "Add resolvers to the Service Provider resource tree;"
  echo "should only be run by members of the gffs-admins group."
  echo "Should be re-run whenever new resources are added:"
  echo
  echo "  $scrip -r -i grid-identity -n resource-name"
  echo
  echo "The grid identity in all of the above is a required parameter and it"
  echo "should be a group that will administrate the SP resources."
  echo
  echo "Examples:"
  echo
  echo "Setting up a TACC resource tree:"
  echo "$scrip -t -i /groups/xsede.org/gffs-admins.tacc \\"
  echo "  -n tacc.xsede.org"
  echo
  echo "Adding a GFFS container:"
  echo "$scrip -g -i /users/admin \\"
  echo "  -h mater.cs.virginia.edu:18443 \\"
  echo "  -n virginia.edu -c virginia-gffs-container"
  echo
  echo "Adding a Unicore BES:" 
  echo "$scrip -u -i /users/admin \\"
  echo "  -h daemon.india.futuregrid.org:8081/INDIA \\"
  echo "  -n india.futuregrid.org -b india-u6-bes"
  echo
  echo "Linking a Unicore BES into a new queue on a GFFS container:"
  echo "$scrip -l -i /groups/xsede.org/fred-admin \\"
  echo "  -n virginia.edu -c virginia-gffs-container -b vt-bes"
  echo
  echo "Replicating the TACC resource tree:"
  echo "$scrip -r -i /groups/xsede.org/gffs-admins.tacc \\"
  echo "  -n tacc.xsede.org"
  echo
}

#################

# variables for major operations.  by default none are enabled.
establish_sp_resource_tree=0
add_sp_container=0
add_unicore_bes=0
link_bes_to_queue=0
mark_for_replication=0

# constants.
top_resources_path="/resources/xsede.org"

##############

# figure out where we are and test some preconditions.

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GENII_USER_DIR" ]; then
  echo "export GENII_USER_DIR before running the script."
  exit 1
fi

if [ -z "$GENII_INSTALL_DIR" ]; then
  echo "export GENII_INSTALL_DIR before running the script."
  exit 1
fi

##############

# process the command line arguments.

if [ $# -eq 0 ]; then
  print_instructions
  exit 0
fi

while getopts ":tgulrb:i:h:n:c:s:" OPTION; do
  case $OPTION in
    t) establish_sp_resource_tree=1 ;;
    g) add_sp_container=1 ;;
    u) add_unicore_bes=1 ;;
    l) link_bes_to_queue=1 ;;
    r) mark_for_replication=1 ;;
    h) the_url="$OPTARG" ;;
    n) sp_name="$OPTARG" ;;
    c) container_name="$OPTARG" ;;
    b) bes_name="$OPTARG" ;;
    s) num_slots="$OPTARG" ;;
    i) grid_identity=$OPTARG
       if [ "$(basename $grid_identity)" == "$grid_identity" ]; then
         grid_identity="/groups/xsede.org/$grid_identity"
       fi
       ;;
    ?) print_instructions
       echo "Option flag -${OPTION} is not supported"
       exit 1
       ;;
  esac
done

#################

# set up some variables we'll need later based on the user's input.
g2_url="https://$the_url/axis/services/VCGRContainerPortType"
u6_url="https://$the_url/services/BESFactory?res=default_bes_factory"
service_provider_path="$top_resources_path/$sp_name"
if [ -z "$num_slots" ]; then num_slots=10; fi

#################

# validate the provided parameters.

# only one major option allowed.
if [ $add_sp_container -eq 0 -a $add_unicore_bes -eq 0 \
       -a $establish_sp_resource_tree -eq 0 \
       -a $link_bes_to_queue -eq 0 \
       -a $mark_for_replication -eq 0 ]; then
  print_instructions
  echo "The command line must specify one of -t, -g, -u, -l, or -r."
  exit 1
fi

# constraints on the resource tree setup option.
if [ $establish_sp_resource_tree -eq 1 ]; then
  if [ $add_sp_container -eq 1 -o $add_unicore_bes -eq 1 \
         -o $link_bes_to_queue -eq 1 ]; then
    print_instructions
    echo "The command line cannot specify -t combined with -l, -g or -u."
    exit 1
  fi
fi

# cannot add an sp container and a unicore container at same time. 
if [ $add_sp_container == 1 -a $add_unicore_bes == 1 ]; then
  print_instructions
  echo "The command line can only specify one of either -g or -u."
  exit 1
fi

# required parameters for adding sp container.
if [ $add_sp_container == 1 ]; then
  if [ -z "$the_url" -o -z "$sp_name" -o -z "$container_name" \
      -o -z "$grid_identity" ]; then
    print_instructions
    echo A required parameter is missing for linking a container into SP tree.
    exit 1
  fi
fi

# required parameters for unicore bes.
if [ $add_unicore_bes == 1 ]; then
  if [ -z "$the_url" -o -z "$sp_name" -o -z "$bes_name" \
      -o -z "$grid_identity" ]; then
    print_instructions
    echo A required parameter is missing for setting up a Unicore BES.
    exit 1
  fi
fi

# required parameters for establishing the sp resource hierarchy.
if [ $establish_sp_resource_tree == 1 ]; then
  if [ -z "$sp_name" -o -z "$grid_identity" ]; then
    print_instructions
    echo A required parameter is missing for setting up the SP resource tree.
    exit 1
  fi
fi

# required parameters for linking a BES to a queue.
if [ $link_bes_to_queue -eq 1 ]; then
  if [ -z "$sp_name" -o -z "$grid_identity" -o -z "$bes_name" \
       -o -z "$container_name" ]; then
    print_instructions
    echo A required parameter is missing for creating a queue with a bes.
    exit 1
  fi
fi

# required parameters for setting up (or reconfiguring) replication for SP tree.
if [ $mark_for_replication -eq 1 ]; then
  if [ -z "$sp_name" -o -z "$grid_identity" ]; then
    print_instructions
    echo A required parameter is missing for creating a queue with a bes.
    exit 1
  fi
fi

##############

# announcing the run.

echo "++++++++++++++++++++++"
echo "GENII_USER_DIR is set to $GENII_USER_DIR"
echo "GENII_INSTALL_DIR is set to $GENII_INSTALL_DIR"
echo "You are logged in as grid user:" 
echo "----------------------"
"$GENII_INSTALL_DIR/grid" whoami
echo "----------------------"
echo "SP tree is at: $service_provider_path"
echo "Identity to give rights to is: $grid_identity"
echo "++++++++++++++++++++++"
echo

##############

if [ $establish_sp_resource_tree == 1 ]; then
  create_resources_structure
fi

if [ $add_sp_container == 1 ]; then
  link_sp_container
fi

if [ $add_unicore_bes == 1 ]; then
  create_unicore_bes
fi

if [ $link_bes_to_queue == 1 ]; then
  create_queue_for_bes  
fi

if [ $mark_for_replication == 1 ]; then
  replicate_resource_hierarchy
fi

##############

