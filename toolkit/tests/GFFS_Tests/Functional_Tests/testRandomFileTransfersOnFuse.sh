#!/bin/bash

# Tests that the file transfer mechanism reliably copies files.
# Author: Chris Koeritz

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.
cd "$WORKDIR"

export SHOWED_SETTINGS_ALREADY=true
if [ -z "$GFFS_TOOLKIT_SENTINEL" ]; then
  source ../../../prepare_tools.sh ../../../prepare_tools.sh 
fi
source "$GFFS_TOOLKIT_ROOT/library/establish_environment.sh"

# we are tracking errors manually here so we can avoid cleaning up if a failure occurred.
error_count=0

# where we hook in the fuse mount.
MOUNT_POINT="$TEST_TEMP/mount-testRandomTransfers"
HOME_PATH_ON_MOUNT="$MOUNT_POINT/$RNSPATH"

function decide_on_filenames_and_sizes()
{
  # constants constraining our test files.
  MAX_TEST_FILES=28
#  MAX_TEST_FILES=3
#hmmm: above just for getting script right.

  # the maximum file we will try to transfer.
  MAX_TEST_SIZE=202749282 # fairly arbitrary size close to 200 mb.

  # set up the filenames we'll use as source material.
  export TESTING_DIR="$TEST_TEMP/transfer_test"
  # recreate our storage directory if it's not there.
  mkdir $TEST_TEMP/transfer_test 2>/dev/null
  EXAMPLE_FILES=()
  local i
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    EXAMPLE_FILES+=("$(mktemp ${TESTING_DIR}/test_file.XXXXXX)")
  done

  echo -n noisy debug of the file list:
  for ((i = 0; i < ${#EXAMPLE_FILES[@]}; i++)); do
    echo -n " ${EXAMPLE_FILES[$i]}"
  done
  echo

  # set up the random size for each file.
  EXAMPLE_SIZES=()
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    # we build up the random value with from 1 to 3 calls to the $RANDOM variable.
    # this gives us three ranges from which the example file sizes will be picked.
    bigrand=
    for ((j = 0; j < $(expr $i % 3) + 1; j++)); do
      bigrand+="$RANDOM"
    done
    tmpsize=$(expr $bigrand % $MAX_TEST_SIZE)
    EXAMPLE_SIZES+=($tmpsize)
  done

#echo "faking the sizes to be quite large"
#EXAMPLE_SIZES=()  #reset the chosen values
#EXAMPLE_SIZES+=(167108860 87108880 127109005)

  echo -n noisy debug of the file sizes:
  for ((i = 0; i < ${#EXAMPLE_SIZES[@]}; i++)); do
    echo -n " ${EXAMPLE_SIZES[$i]}"
  done
  echo
}

function copyOneFileUp()
{
  local filename="$1"; shift
  local size="$1"; shift

  local base="$(basename "$filename")"
  local fusePath="$HOME_PATH_ON_MOUNT/$base"

#echo "filename=$filename size=$size base=$base fusePath=$fusePath"

  timed_command cp "$filename" "$fusePath"
  retval=$?
  assertEquals "Copying local file $filename" 0 $retval
  if [ $? -ne 0 ]; then ((error_count++)); fi
  if [ $retval -eq 0 ]; then
    real_time=$(calculateTimeTaken)
    echo "Time taken to copy $filename with $size bytes is $real_time s"
    actual_size=$(\ls -l $filename | awk '{print $5}')
    showBandwidth "$real_time" $size
  fi
}

function copyOneFileDown()
{
  local filename="$1"; shift
  local size="$1"; shift

  local base="$(basename "$filename")"
  local fusePath="$HOME_PATH_ON_MOUNT/$base"
  local newLocal="${filename}.new"

#echo "filename=$filename size=$size base=$base fusePath=$fusePath newLocal=$newLocal"

  timed_command cp "$fusePath" "$newLocal"
  retval=$?
  assertEquals "Copying remote file $fusePath" 0 $retval
  if [ $? -ne 0 ]; then ((error_count++)); fi
  if [ $retval -eq 0 ]; then
    real_time=$(calculateTimeTaken)
    echo "Time taken to copy $fusePath with $size bytes is $real_time s"
    actual_size=$(\ls -l $filename | awk '{print $5}')
    showBandwidth "$real_time" $size
  fi
}

function compareBeforeAndAfter()
{
  local filename="$1"; shift
  local size="$1"; shift

  local newLocal="${filename}.new"

  local md5Orig="$(md5sum "$filename" | awk '{print $1}')"
  assertEquals "Computing md5sum for $filename" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi
  local md5New="$(md5sum "$newLocal" | awk '{print $1}')"
  assertEquals "Computing md5sum for $newLocal" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi
  local sizeOrig="$(stat -c "%s" "$filename")"
  assertEquals "Computing size for $filename" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi
  local sizeNew="$(stat -c "%s" "$newLocal")"
  assertEquals "Computing size for $newLocal" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi

echo "md5Orig=$md5Orig md5New=$md5New sizeOrig=$sizeOrig sizeNew=$sizeNew"

  test "$md5Orig" == "$md5New"
  assertEquals "Agreement of md5sums for before and after" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi

  test "$sizeOrig" -eq "$sizeNew"
  assertEquals "Agreement of size for before and after" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi

  test "$sizeOrig" -eq "$size"
  assertEquals "Size correct for before file" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi
  test "$sizeNew" -eq "$size"
  assertEquals "Size correct for after file" 0 $?
  if [ $? -ne 0 ]; then ((error_count++)); fi
}

##############

oneTimeSetUp()
{
  sanity_test_and_init  # make sure test environment is good.

  # clean up from last test run.
  oneTimeTearDown

  # pick some file names and sizes for testing.
  decide_on_filenames_and_sizes

  if ! fuse_supported; then return 0; fi
  # need to just go for it and try to unmount; the directory left for a dead mount
  # is still present but is not seen by checks.
  fusermount -u "$MOUNT_POINT" &>/dev/null
  sync ; sleep 2
  if [ -d "$MOUNT_POINT" ]; then
    rmdir "$MOUNT_POINT"
  fi
  mkdir "$MOUNT_POINT"
}

testFuseMounting()
{
  if ! fuse_supported; then return 0; fi

  echo "Mounting grid with fuse filesystem at: $MOUNT_POINT"
  fuse --mount local:"$MOUNT_POINT"
  sleep 30

  test_fuse_mount "$MOUNT_POINT"
  check_if_failed "Mounting grid to local directory"
}

testCreateFiles()
{
  local i
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    local file="${EXAMPLE_FILES[$i]}"
    local size="${EXAMPLE_SIZES[$i]}"
    createRandomFile "$file" $size
  done
}

testCopyFilesUp()
{
  local i
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    copyOneFileUp "${EXAMPLE_FILES[$i]}" "${EXAMPLE_SIZES[$i]}"
  done
}

testCopyFilesDown()
{
  local i
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    copyOneFileDown "${EXAMPLE_FILES[$i]}" "${EXAMPLE_SIZES[$i]}"
  done
}

testFilesCameOutOkay()
{
  local i
  for ((i = 0; i < $MAX_TEST_FILES; i++)); do
    echo -e "\nchecking file ${EXAMPLE_FILES[$i]}..."
    compareBeforeAndAfter "${EXAMPLE_FILES[$i]}" "${EXAMPLE_SIZES[$i]}"
  done
}

testUnmountingFuse()
{
  if ! fuse_supported; then return 0; fi
  cd "$WORKDIR"  # change dir off fuse mount.
  grid fuse --unmount local:"$MOUNT_POINT"
  retval=$?
  sleep 30
  assertEquals "Unmounting fuse grid mount" 0 $retval
  rmdir "$MOUNT_POINT"
  assertEquals "Checking that directory is no longer mounted" 0 $?
}

oneTimeTearDown()
{
  # check our error count to see if it was ever incremented.
  if [[ $error_count == 0 ]]; then
    # no errors, so clean up.
  if [ ${#EXAMPLE_FILES[@]} -gt 0 ]; then
    gridnames=()
    for ((i = 0; i < ${#EXAMPLE_FILES[@]}; i++)); do
      gridnames+=("$RNSPATH/$(basename "${EXAMPLE_FILES[$i]}")")
    done
      echo "cleaning grid files: ${gridnames[@]}"
      grid rm "${gridnames[@]}"
  fi
    echo "cleaning local transfer directory $TESTING_DIR"
    rm -rf "$TESTING_DIR"
  else 
    # something failed, so leave the wreckage visible.
    echo "*** cleaning turned off due to $error_count errors during test run!"
  fi
}

# load and run shUnit2
source "$SHUNIT_DIR/shunit2"

