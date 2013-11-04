#!/bin/bash

if [ ! -d "$GENII_INSTALL_DIR" -o ! -d "$GENII_USER_DIR" ]; then
  echo "The GENII_INSTALL_DIR or GENII_USER_DIR directories do not exist."
  echo "They are required for this script to backup the GenesisII user state directory."
  exit 1
fi

backup_dir="$1"; shift
if [ -z "$backup_dir" ]; then
  echo "No directory for storing the backup was provided; defaulting to HOME."
  backup_dir="$HOME"
fi

# pick a file for the container backup.
backup_file="$backup_dir/container-state-$(hostname)-backup-$(date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/').tar.gz"

# there's a possible conflict if an auto-start procedure will crank up the
# container again, so we move the startup script out of the way.
mv "$GENII_INSTALL_DIR/GFFSContainer" "$GENII_INSTALL_DIR/GFFSContainer.hold"

# stop the container.
"$GENII_INSTALL_DIR/GFFSContainer.hold" stop

additional_pax=()
if [ -f "$GENII_INSTALL_DIR/context.xml" ]; then 
  additional_pax+=("$GENII_INSTALL_DIR/context.xml")
fi

# backup the container state.
tar -czf "$backup_file" "$GENII_USER_DIR" "$GENII_INSTALL_DIR/deployments" "$GENII_INSTALL_DIR/container.properties" "${additional_pax[@]}"

if [ ! -z "$BACKUP_INTO_GRID" ]; then
  #copy the backup state to grid-namespace
  "$GENII_INSTALL_DIR/grid cp local:$backup_file grid:/etc/backups/"
fi

# move the starter script back into place.
mv "$GENII_INSTALL_DIR/GFFSContainer.hold" "$GENII_INSTALL_DIR/GFFSContainer"

# start the container.
"$GENII_INSTALL_DIR/GFFSContainer" start

