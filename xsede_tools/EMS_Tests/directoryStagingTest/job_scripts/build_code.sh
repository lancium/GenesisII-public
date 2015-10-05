#!/bin/bash

# get the two parameters we need to know.
source_directory="$1"; shift
target_directory="$1";shift

if [ -z "$source_directory" -o -z "$target_directory" ]; then
  echo "This script requires two parameters; the first is a source location for the codebase, and the second is a target location to copy the resulting output created by the build process."
  exit 1
fi

echo "going to build in '$source_directory' and copy results to '$target_directory'"

# clean first.
\rm -rf "$source_directory"

# then make sure the source directory exists.
mkdir "$source_directory"
if [ $? -ne 0 ]; then
  echo failed to make the source directory and it does not already exist.
  exit 1
fi

pushd "$source_directory"
if [ $? -ne 0 ]; then
  echo failed to change to the source directory.
  exit 1
fi

make
if [ $? -ne 0 ]; then
  echo "the build process failed (coming as stage-in directory)."
  exit 1
else
  echo "no errors were noticed during the build process.  yay."
fi

popd

echo ===================================
echo "here's what's in the source directory before copying..."
ls -alr "$source_directory" 
echo ===================================

# now copy the products to the target location.
cp -r "$source_directory" "$target_directory"
retval=$?

if [ $retval -ne 0 ]; then
  echo "there was an error during the copy (directory stage-out) of the build products."
else
  echo "no errors were noticed during copy.  whew."
fi

exit $retval

