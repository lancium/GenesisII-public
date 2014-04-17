#!/bin/bash

# Processes JSDL files by removing "well-known" patterns and replacing them with the
# appropriate data for testing.
#
# Author: Chris Koeritz
# Author: Bastian Demuth

create_jsdl_from_templates()
{
  if [ ! -d "$GENERATED_JSDL_FOLDER" ]; then
    # make sure we have our target folder.
    mkdir -p "$GENERATED_JSDL_FOLDER"
  fi
  # patching user name in the jsdl input files...
  local FPATH=$RNSPATH
  echo RNSPATH is $FPATH
  export REAL_LENGTH=$(expr length $FPATH)

  local i
  local j
  for i in *.jsdl; do
    echo Patching $i
    gawk -v pl=$REAL_LENGTH 'BEGIN { plength = pl }; \
!/PLENGTH/ { print }; \
/PLENGTH/ { \
  match($0, /(.*)(PLENGTH)[[:space:]]*(\+[[:space:]]*([[:digit:]]+))?(.*)/, arr); \
  printf "%s%d%s\n", arr[1], plength+arr[4], arr[5] \
};' $i >"$GENERATED_JSDL_FOLDER/$i"
    sed -i -e 's%PATH%'$FPATH'%g' \
      -e 's%EMAIL%'$EMAIL'%g' \
      -e 's%SPMD_VARIATION%'$SPMD_VARIATION'%g' \
      "$GENERATED_JSDL_FOLDER/$i"
    # note that SFTP and GRIDFTP have to go first so we don't erroneously match FTP instead.
    for j in SFTP GRIDFTP FTP HTTP SCP; do
      indirect="$j[*]"
#echo "value of array named $j is ${!indirect}"
      # turn the indirect reference into a nicer array variable.
      declare -a arrrh=(${!indirect})
#echo "value of array named arrrh is ${arrrh[*]}"
      host_string="${j}_HOSTNAME"
      dir_string="${j}_TESTDIR"
      name_string="${j}_USERNAME"
      passwd_string="${j}_PASSWORD"
#echo "vars are: $host_string $dir_string $name_string $passwd_string "
      sed -i -e "s%$host_string%${arrrh[0]}%" \
          -e "s%$dir_string%${arrrh[1]}%" \
          -e "s%$name_string%${arrrh[2]}%" \
          -e "s%$passwd_string%${arrrh[3]}%" \
          "$GENERATED_JSDL_FOLDER/$i"
    # add in any random values requested.
#does not work yet; so far cannot find way to pass the matched component (\1) into the 
#  subshell to call our function.  lots of other ways suggest themselves but this is not
#  a priority yet.
#    sed -i -e "s%RANDOM(\([^)]*\))%`get_random_id \1`%g" \
#      "$GENERATED_JSDL_FOLDER/$i"

    # fix the binary path to point at cygwin if this is windows.
    if [ "$OS" == "Windows_NT" ]; then
      if [ -z "$CYGWIN_BIN_PATH" ]; then
        echo The cygwin binary path is not defined.  On windows, this is required
        echo to patch the JSDL files so that the BES can find the cygwin binaries.
        echo In the XSEDE tools config file, please set the CYGWIN_BIN_PATH
        echo variable to the Cygwin tools bin directory.
        exit 1
      fi
#echo unmod path=$CYGWIN_BIN_PATH
      local modified_path=$(echo "$CYGWIN_BIN_PATH" | sed -e 's/\//\\/g' | sed -e 's/\\/\\\\/g')
#echo modifpath=$modified_path
      sed -i -e "s%\/bin\/\([a-zA-Z0-9]*\)%$modified_path\\\\\1.exe%" "$GENERATED_JSDL_FOLDER/$i"
    fi

    # check for any error code coming from the random id insertions.
    grep "FAILURE-to-find" "$GENERATED_JSDL_FOLDER/$i"
    if [ $? -eq 0 ]; then
      echo "FAILURE: random id replacement failure, undefined value used?"
      return 1
    fi
    done
  done
}

# makes N copies of a jsdl file, where the key phrase INDEX inside the file is replaced
# with the index number for which copy the file is.
function replicate_jsdl_file()
{
  filename="$1"; shift
  count="$1"; shift
  if [ -z "$filename" -o -z "$count" ]; then
    echo This function needs a filename to replicate, and a count of how many copies to make.
    return 1
  fi
  for ((i=0; i < $count; i++)); do
    cat "$filename".jsdl | sed -e "s/INDEX/$i/g" >"$filename-$i".jsdl
  done
}

