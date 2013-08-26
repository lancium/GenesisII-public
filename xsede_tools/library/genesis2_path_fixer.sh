#!/bin/bash

# fixes the paths in a pre-built version of Genesis II to use a different path.
# this only effective if done before bootstrapping a container using the target directory.

current_path="$1"; shift
old_location="$1"; shift

if [ -z "$current_path" -o -z "$old_location" ]; then
  echo This script needs two parameters:
  echo "  (1) the path where the Genesis II code exists on this host."
  echo "  (2) the path where the Genesis II code was built."
  echo "Given those paths, the script will replace all occurrences of the"
  echo "latter with the former."
  exit 1
fi

if [ ! -d "$current_path" ]; then
  echo The directory given for the current path does not seem to exist.
  exit 1
fi
if [ ! -f "$current_path/runContainer.sh" -a ! -f "$current_path/XCGContainer" \
    -a ! -f "$current_path/grid" ]; then
  echo The directory given for the current path does not seem to be a Genesis II
  echo directory that has been built appropriately.  For example, there is no
  echo runContainer.sh or XCGContainer or grid script available.
  exit 1
fi

replacement_path=$(echo $current_path | sed -e 's/\/*$//g' | sed -e 's/\//\\\//g')
seeking_path=$(echo $old_location | sed -e 's/\/*$//g' | sed -e 's/\//./g')

path_fixer=$(mktemp $TMP/path_fixer.XXXXXX)

echo "if [[ ! \"\$1\" =~ .*\\.jar$ ]]; then sed -i -e 's/$seeking_path/$replacement_path/g' \$1; if [ \$? -eq 0 ]; then echo \"fixing \$1\" ; fi ; fi" >$path_fixer

# fix the root of the build.
find "$current_path" -maxdepth 1 -type f -exec bash "$path_fixer" "{}" ';'
# fix the application watcher's directory.
find "$current_path/ApplicationWatcher" -maxdepth 1 -type f -exec bash "$path_fixer" "{}" ';'

echo "All paths should have been replaced."

# now that we're done, clean up.
rm "$path_fixer"


