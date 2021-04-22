#!/bin/bash

# Author: Charlie Houghton
# Date: 2021 April 16
# This file is mainly to check that the overlay tool works with "large" files.
# Previously, Apache axis was failing because transfer blocks were > 16K,
# and failed to write to disk because of a permissions error.
# I chose 35 MB for the file size because we're transfering in 32 MB blocks,
# and if there was a problem with the block size this test might catch it.
#
# This is NOT a complete test of overlay. I still need to verify overlay works with non-zero
# offsets for each use case (local -> local, local -> grid, grid -> grid, grid -> local).

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then echo Please run prepare_tools.sh before testing.; exit 3; fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

MOUNT_POINT="$TEST_TEMP/mount-gffsGridCommands"

test_file_sum="default sum"
test_file_size="default size"

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # Setup
  fallocate -l 35M test.file
  test_file_sum="$(md5sum test.file | awk '{print $1}')"
  test_file_size="$(du -b test.file | awk '{print $1}')"

  touch local-local.file
  touch local-grid.file
  touch grid-grid.file
  touch grid-local.file

  grid touch local-grid.file grid-grid.file

  grid cp local:test.file test.file

  echo -n abcd > src.txt
  echo -n 12345678 > local-local-dest.txt
  cp local-local-dest.txt grid-local-dest.txt
  grid cp local:src.txt src.txt
  grid cp local:local-local-dest.txt grid-grid-dest.txt
  grid cp local:local-local-dest.txt local-grid-dest.txt

}

testSmallLocalToLocal()
{
  grid overlay local:src.txt local:local-local-dest.txt 2
  assertEquals "Testing small local -> local overlay with 2 offset for completion" 0 $?

  overlay_contents="$(cat local-local-dest.txt)"
  echo overlay_contents: $overlay_contents
  echo $overlay_contents | grep -q "12abcd78"
  assertEquals "Testing small local -> local overlay with 2 offset for accuracy (contents)" 0 $?

  overlay_size="$(du -b local-local-dest.txt | awk '{print $1}')"
  echo $overlay_size | grep -q "8"
  assertEquals "Testing small local -> local overlay with 2 offset for accuracy (file size)" 0 $?
}

testSmallLocalToGrid()
{
  grid overlay local:src.txt local-grid-dest.txt 2
  grid cp local-grid-dest.txt local:local-grid-dest.txt

  assertEquals "Testing small local -> grid overlay with 2 offset for completion" 0 $?

  overlay_contents="$(cat local-grid-dest.txt)"
  echo overlay_contents: $overlay_contents
  echo $overlay_contents | grep -q "12abcd78"
  assertEquals "Testing small local -> grid overlay with 2 offset for accuracy (contents)" 0 $?

  overlay_size="$(du -b local-grid-dest.txt | awk '{print $1}')"
  echo $overlay_size | grep -q "8"
  assertEquals "Testing small local -> grid overlay with 2 offset for accuracy (file size)" 0 $?
}

testSmallGridToGrid()
{
  grid overlay src.txt grid-grid-dest.txt 2
  grid cp grid-grid-dest.txt local:grid-grid-dest.txt

  assertEquals "Testing small grid -> grid overlay with 2 offset for completion" 0 $?

  overlay_contents="$(cat grid-grid-dest.txt)"
  echo overlay_contents: $overlay_contents
  echo $overlay_contents | grep -q "12abcd78"
  assertEquals "Testing small grid -> grid overlay with 2 offset for accuracy (contents)" 0 $?

  overlay_size="$(du -b grid-grid-dest.txt | awk '{print $1}')"
  echo $overlay_size | grep -q "8"
  assertEquals "Testing small grid -> grid overlay with 2 offset for accuracy (file size)" 0 $?
}

testSmallGridToLocal()
{
  grid overlay src.txt local:grid-local-dest.txt 2

  assertEquals "Testing small grid -> local overlay with 2 offset for completion" 0 $?

  overlay_contents="$(cat grid-local-dest.txt)"
  echo overlay_contents: $overlay_contents
  echo $overlay_contents | grep -q "12abcd78"
  assertEquals "Testing small grid -> local overlay with 2 offset for accuracy (contents)" 0 $?

  overlay_size="$(du -b grid-local-dest.txt | awk '{print $1}')"
  echo $overlay_size | grep -q "8"
  assertEquals "Testing small grid -> local overlay with 2 offset for accuracy (file size)" 0 $?
}

testLargeLocalToLocal()
{
  grid overlay local:test.file local:local-local.file 0
  assertEquals "Testing large local -> local overlay with 0 offset for completion" 0 $?

  overlay_sum="$(md5sum local-local.file | awk '{print $1}')"
  echo test_file_sum: $test_file_sum
  echo overlay_sum: $overlay_sum
  echo $test_file_sum | grep -q $overlay_sum
  assertEquals "Testing large local -> local overlay with 0 offset for accuracy (md5sum)" 0 $?

  overlay_size="$(du -b local-local.file | awk '{print $1}')"
  echo $test_file_size | grep -q $overlay_size
  assertEquals "Testing large local -> local overlay with 0 offset for accuracy (file size)" 0 $?
}

testLargeLocalToGrid()
{
  grid overlay local:test.file local-grid.file 0
  assertEquals "Testing large local -> grid overlay with 0 offset for completion" 0 $?
  grid cp local-grid.file local:local-grid.file

  overlay_sum="$(md5sum local-grid.file | awk '{print $1}')"
  echo test_file_sum: $test_file_sum
  echo overlay_sum: $overlay_sum
  echo $test_file_sum | grep -q $overlay_sum
  assertEquals "Testing large local -> grid overlay with 0 offset for accuracy (md5sum)" 0 $?

  overlay_size="$(du -b local-grid.file | awk '{print $1}')"
  echo $test_file_size | grep -q $overlay_size
  assertEquals "Testing large local -> grid overlay with 0 offset for accuracy (file size)" 0 $?
}

testLargeGridToGrid()
{
  grid overlay test.file grid-grid.file 0
  assertEquals "Testing large local -> grid overlay with 0 offset for completion" 0 $?
  grid cp grid-grid.file local:grid-grid.file

  overlay_sum="$(md5sum grid-grid.file | awk '{print $1}')"
  echo test_file_sum: $test_file_sum
  echo overlay_sum: $overlay_sum
  echo $test_file_sum | grep -q $overlay_sum
  assertEquals "Testing large grid -> grid overlay with 0 offset for accuracy (md5sum)" 0 $?

  overlay_size="$(du -b grid-grid.file | awk '{print $1}')"
  echo $test_file_size | grep -q $overlay_size
  assertEquals "Testing large grid -> grid overlay with 0 offset for accuracy (file size)" 0 $?
}

testLargeGridToLocal()
{
  grid overlay test.file local:grid-local.file 0
  assertEquals "Testing large grid -> local overlay with 0 offset for completion" 0 $?

  overlay_sum="$(md5sum grid-local.file | awk '{print $1}')"
  echo test_file_sum: $test_file_sum
  echo overlay_sum: $overlay_sum
  echo $test_file_sum | grep -q $overlay_sum
  assertEquals "Testing large grid -> local overlay with 0 offset for accuracy (md5sum)" 0 $?

  overlay_size="$(du -b grid-local.file | awk '{print $1}')"
  echo $test_file_size | grep -q $overlay_size
  assertEquals "Testing large grid -> local overlay with 0 offset for accuracy (file size)" 0 $?
}

oneTimeTearDown() {
  rm local-local.file
  rm local-grid.file
  rm grid-grid.file
  rm grid-local.file
  rm test.file
  rm src.txt
  rm local-local-dest.txt
  rm local-grid-dest.txt
  rm grid-grid-dest.txt
  rm grid-local-dest.txt

  # All one line to avoid needing to create a bunch of grid shells
  grid rm test.file local-grid.file grid-grid.file src.txt local-grid-dest.txt grid-grid-dest.txt

}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"
