#!/bin/bash

# This test will verify that an export is operational for read and write.
# It requires two parameters: (1) a local system path where the export actually
# lives and the corresponding GFFS path in the grid where that export is visible.
# The unix user who runs this script must have visibility of the path in (1).
# The tester running the script must also be logged into the GFFS as the owner
# of the export.  The installation should be located at $GENII_INSTALL_DIR
# (with state directory in $GENII_USER_DIR), which can be established by
# running the included script set_gffs_vars, e.g.:
#    source ~/GenesisII/set_gffs_vars
# Each step of the test must succeed for the export to be considered viable.
#
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

source "../../prepare_tools.sh" "../../prepare_tools.sh"
if [ -z "$TEST_TEMP" ]; then
  echo The xsede tool suite could not be automatically located.
  exit 1
fi

if [ -z "$XSEDE_TEST_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi


#hmmm: turn this into a set of shunit tests!!!

local_path="$1"; shift
grid_path="$1"; shift

function print_instructions()
{
  echo
  echo "This test will verify that an export is operational for read and write."
  echo "It requires two parameters: (1) a local system path where the export actually"
  echo "lives and the corresponding GFFS path in the grid where that export is visible."
  echo "The unix user who runs this script must have read and write access on the path"
  echo "in (1).  The tester running the script must also be logged into the GFFS as"
  echo "the owner of the export.  The installation should be located at the value of"
  echo "the \$GENII_INSTALL_DIR environment variable (and should have a state directory"
  echo "in \$GENII_USER_DIR).  The environment variables can be established by running"
  echo "the script set_gffs_vars, which is included in the install.  For example:"
  echo "   source ~/GenesisII/set_gffs_vars"
  echo "Each step of the test must succeed for the export to be considered viable."
  echo
}

if [ -z "$local_path" ]; then
  print_instructions
  echo "No local path was provided for the export origin."
  exit 1
fi
if [ ! -d "$local_path" ]; then
  print_instructions
  echo "The local path provided does not actually exist, and it must."
  exit 1
fi
if [ -z "$grid_path" ]; then
  print_instructions
  echo "No grid path was provided for where the export resides in GFFS."
  exit 1
fi

#source "$XSEDE_TEST_ROOT/library/establish_environment.sh"

echo -e "\n\n0. Set up a data file for use in the export testing."
# make a temporary file that we can ship around.
temp_file="$(mktemp "$TEST_TEMP/export-test-data-file.XXXXXX")"
check_if_failed "creating temporary file"

# write random data to the temp file.  this will just put 5K into it.
dd if=/dev/urandom of=$temp_file bs=1 count=5120
check_if_failed "writing random data into temporary file"

echo "step 0 success."

echo -e "\n\n1. List the new export path.  If there are no files in the exported directory, then none will show up yet, but no errors should be returned either."
grid ls "$grid_path"
check_if_failed "listing the export path"

echo "step 1 success."

echo -e "\n\n2. Copy a file from the local filesystem into the local export folder (not in the grid).  This will provide a starting file that should be visible."
short_version="file-testing-export"
local_file="$local_path/$short_version"
cp "$temp_file" "$local_file"
check_if_failed "copying a local file into the local export path"

echo "step 2 success."

echo -e "\n\n3. Now list the export again in the grid.  The copied file should show up in the listing."
outfile1="$(mktemp "$TEST_TEMP/export-test-data-file.XXXXXX")"
grid ls "$grid_path" &>"$outfile1"
check_if_failed "listing the export path with new file in it"
# make sure file is actually there now.
grep -q "$short_version" <"$outfile1"
check_if_failed "seeking new file name in listing of export"

echo "step 3 success."

echo -e "\n\n4. Copy a file from elsewhere in the grid into the export."
# first make sure we know of another file we can use.
shortaltname="alternate-export-test-file"
othergridfile="$grid_path/$shortaltname"
grid cp local:"$temp_file" "$othergridfile"
check_if_failed "preparing a file within the grid at: $othergridfile"
# now that we have a known quantity, we will copy that from within the grid
# to the export path in the grid, which should make the file show up on
# the local path also.
grid cp "$othergridfile" "$grid_path"
check_if_failed "copying a file from within the grid onto the export"

echo "step 4 success."

echo -e "\n\n5. List the local export path to ensure the file showed up."
outfile2="$(mktemp "$TEST_TEMP/export-test-data-file.XXXXXX")"
grid ls "$grid_path" &>"$outfile2"
check_if_failed "listing the export path after copying file into it from the grid"
# make sure file is actually there now.
grep -q "$shortaltname" <"$outfile2"
check_if_failed "seeking newly copied within-grid file in listing of export"
# test also that the file showed up locally.
ls "$local_path" &>"$outfile2"
check_if_failed "listing the local path after copying file into export from the grid"
grep -q "$shortaltname" <"$outfile2"
check_if_failed "seeking newly copied file in listing of local path"

echo "step 5 success."

echo -e "\n\n6. Modify the file locally to make easily identifiable changes in the file."
# this drops another copy of the test data at the end of the file.
cat "$temp_file" >>"$local_path/$shortaltname" 
check_if_failed "modifying data file contents via local path"

echo "step 6 success."

echo -e "\n\n7. Print out the file from within the grid, showing that the grid sees the modified file."
temp_file2="$(mktemp "$TEST_TEMP/export-test-data-file.XXXXXX")"
grid cp "$othergridfile" local:"$temp_file2"
check_if_failed "copying the modified data file out of the grid"
diff -s "$local_path/$shortaltname" "$temp_file2"
check_if_failed "checking if the modified local file and grid copy are the same"

echo "step 7 success."

echo -e "\n\n8. Modify the file within the grid side this time.  We will just overwrite the file."
# this resets the file to a single copy of our test data.
grid cp local:"$temp_file" "$othergridfile"
check_if_failed "overwriting data file within grid"

echo "step 8 success."

echo -e "\n\n9. Print the file's current version again, this time from the local file system.  This should show the new contents (rather than the previously edited contents)."
diff -s "$local_path/$shortaltname" "$temp_file"
check_if_failed "checking if the local file and modified grid copy are the same"

echo "step 9 success."

echo -e "\n\n\nThe export has been tested and seems fully functional for reading and writing.\n\n"

# clean up
rm -f "$temp_file" "$temp_file2" "$outfile1" "$outfile2" "$local_path/$shortaltname" "$local_file"


