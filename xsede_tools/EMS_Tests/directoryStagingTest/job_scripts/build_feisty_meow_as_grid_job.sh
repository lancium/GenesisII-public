#!/bin/bash

# get the two parameters we need to know.
source_directory="$1"; shift
target_directory="$1";shift

if [ -z "$source_directory" -o -z "$target_directory" ]; then
  echo "This script requires two parameters; the first is a source location for the feisty meow codebase, and the second is a target location to copy the resulting production directory created by the feisty meow build process."
  exit 1
fi

echo "going to build in '$source_directory' and copy results to '$target_directory'"

# make sure we don't carry anyone else's config in here.
unset FEISTY_MEOW_DIR

# make sure the source directory exists.
if [ ! -d "$source_directory" ]; then
  mkdir "$source_directory"
  if [ $? -ne 0 ]; then
    echo failed to make the source directory and it does not already exist.
    exit 1
  fi
fi

cd "$source_directory"
if [ $? -ne 0 ]; then
  echo failed to change to the source directory.
  exit 1
fi

# make sure no existing feisty meow directory is present.
rm -rf "./feisty_meow"


####
#echo "bailing out of normal process here, just making simple copy"
#mkdir $target_directory
#echo >"$target_directory/binaries/nechung" <<eof
#echo this is not really nechung.
#echo nechung variable is \$NECHUNG.
#eof
#echo end of gunk temp
#exit 0
####

# get the feisty meow codebase now.
git clone git://feistymeow.org/feisty_meow

bash "$source_directory/feisty_meow/scripts/generator/bootstrap_build.sh"
if [ $? -ne 0 ]; then
  echo "the feisty meow build process failed (coming as stage-in directory)."
  exit 1
else
  echo "no errors were noticed during the build process.  yay."
fi

# now copy the products to the target location.
cp -r "$source_directory/feisty_meow/production" "$target_directory"
retval=$?

if [ $retval -ne 0 ]; then
  echo "there was an error during the copy (directory stage-out) of the build products."
else
  echo "no errors were noticed during copy.  whew."
fi

exit $retval

