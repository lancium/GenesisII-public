#!/bin/bash

# a library of functions used by the install scripts.

##############

# given a file name and a phrase to look for, this replaces all instances of
# it with a piece of replacement text.  note that slashes are okay in the two
# text pieces, but we are using pound signs as the regular expression
# separator; phrases including the octothorpe (#) will cause syntax errors.
function replace_phrase_in_file()
{
  local file="$1"; shift
  local phrase="$1"; shift
  local replacement="$1"; shift
  if [ -z "$file" -o ! -f "$file" -o -z "$phrase" -o -z "$replacement" ]; then
    echo "replace_phrase_in_file: needs a filename, a phrase to replace, and the"
    echo "text to replace that phrase with."
    return 1
  fi
  sed -i -e "s%$phrase%$replacement%g" "$file"
}

# similar to replace_phrase_in_file, but also will add the new value
# when the old one did not already exist in the file.
function replace_if_exists_or_add()
{
  local file="$1"; shift
  local phrase="$1"; shift
  local replacement="$1"; shift
  if [ -z "$file" -o ! -f "$file" -o -z "$phrase" -o -z "$replacement" ]; then
    echo "replace_if_exists_or_add: needs a filename, a phrase to replace, and the"
    echo "text to replace that phrase with."
    return 1
  fi
  grep "$phrase" "$file" >/dev/null
  # replace if the phrase is there, otherwise add it.
  if [ $? -eq 0 ]; then
    replace_phrase_in_file "$file" "$phrase" "$replacement"
  else
    # this had better be the complete line.
    echo "$replacement" >>"$file"
  fi
}

##############

# finds a variable (first parameter) in a particular property file
# (second parameter).  the expected format for the file is:
# varX=valueX
function seek_variable()
{
  local find_var="$1"; shift
  local file="$1"; shift
  if [ -z "$find_var" -o -z "$file" -o ! -f "$file" ]; then
    echo -e "seek_variable: needs two parameters, firstly a variable name, and\nsecondly a file where the variable's value will be sought." 1>&2
    return 1
  fi

  while read line; do
    if [ ${#line} -eq 0 ]; then continue; fi
    # split the line into the variable name and value.
    IFS='=' read -a assignment <<< "$line"
    local var="${assignment[0]}"
    local value="${assignment[1]}"
    if [ "${value:0:1}" == '"' ]; then
      # assume the entry was in quotes and remove them.
      value="${value:1:$((${#value} - 2))}"
    fi
    if [ "$find_var" == "$var" ]; then
      echo "$value"
    fi
  done < "$file"
}

# finds a variable (first parameter) in a particular XML format file
# (second parameter).  the expected format for the file is:
# ... name="varX" value="valueX" ...
function seek_variable_in_xml()
{
  local find_var="$1"; shift
  local file="$1"; shift
  if [ -z "$find_var" -o -z "$file" -o ! -f "$file" ]; then
    echo "seek_variable_in_xml: needs two parameters, firstly a variable name, and"
    echo "secondly an XML file where the variable's value will be sought."
    return 1
  fi

  while read line; do
    if [ ${#line} -eq 0 ]; then continue; fi
    # process the line to make it more conventional looking.
    line="$(echo "$line" | sed -e 's/.*name="\([^"]*\)" value="\([^"]*\)"/\1=\2/')"
    # split the line into the variable name and value.
    IFS='=' read -a assignment <<< "$line"
    local var="${assignment[0]}"
    local value="${assignment[1]}"
    if [ "${value:0:1}" == '"' ]; then
      # assume the entry was in quotes and remove them.
      value="${value:1:$((${#value} - 2))}"
    fi
    if [ "$find_var" == "$var" ]; then
      echo "$value"
    fi
  done < "$file"
}

# locates a compiler variable in the installation's configuration files.
# this only works properly with a newer interactive or linux package
# installer and will fail with the older interactive installer (SD&I
# activity 126 and earlier).
function retrieve_compiler_variable()
{
  local find_var="$1"; shift
  if [ -z "$find_var" ]; then
    echo "retrieve_compiler_variable: needs a variable name that will be sought in the"
    echo "current.deployment and current.version files in an installation."
    return 1
  fi

  if [ ! -f "$GENII_INSTALL_DIR/current.deployment" \
      -o ! -f "$GENII_INSTALL_DIR/current.version" ]; then
    echo "Either the current.deployment or the current.version file could not be located"
    echo "in the existing installation.  This is most likely because this installation"
    echo "was created with the 2.7.499 installer or earlier.  Please upgrade to the latest"
    echo "Genesis 2.7.500+ interactive installer before proceeding."
    return 1
  fi

  local combo_file="$(mktemp /tmp/$USER-temp-instinfo.XXXXXX)"
  cat "$GENII_INSTALL_DIR/current.deployment" >>"$combo_file"
  cat "$GENII_INSTALL_DIR/current.version" >>"$combo_file"

  local value="$(seek_variable "$find_var" "$combo_file")"

#done already.  # fix any quoting.
#  if [ "${value:0:1}" == '"' ]; then
#    # assume the entry was in quotes and remove them.
#    value="${value:1:$((${#value} - 2))}"
#  fi

  echo "$value"

  rm -f "$combo_file"
}

##############

function replace_compiler_variables()
{
  local file="$1"; shift

  local combo_file="$(mktemp /tmp/$USER-temp-instinfo.XXXXXX)"
  cat "$GENII_INSTALL_DIR/current.deployment" >>"$combo_file"
  cat "$GENII_INSTALL_DIR/current.version" >>"$combo_file"

  # replace installer variables in files.
  while read line; do
    if [ ${#line} -eq 0 ]; then continue; fi
    #echo got line to replace: $line
    # split the line into the variable name and value.
    IFS='=' read -a assignment <<< "$line"
    local var="${assignment[0]}"
    local value="${assignment[1]}"
    if [ "${value:0:1}" == '"' ]; then
      # assume the entry was in quotes and remove them.
      value="${value:1:$((${#value} - 2))}"
    fi
    #echo read var $var and value $value
    local seeking="\${compiler:$var}"
    local replacement="$value"
    replace_phrase_in_file "$file" "$seeking" "$replacement"
  done < "$combo_file"

  \rm -f "$combo_file"
}

function replace_installdir_variables()
{
  # the passed in directory is not only where we operate, it is also
  # the value we will be replacing install4j references with.
  local dir="$1"; shift
  local fname

  for fname in $(find $dir -type f ! -iname "*.sh" -exec grep -l installer:sys.installationDir {} ';'); do
    local seeking="\${installer:sys.installationDir}"
    local replacement="$dir"
    replace_phrase_in_file "$fname" "$seeking" "$replacement"
  done
}

##############

function generate_cert()
{
  local file="$1"; shift
  local passwd="$1"; shift

  "$GENII_INSTALL_DIR/grid" cert-generator --gen-cert --keysize=2048 "--ks-path=$file" "--ks-pword=$passwd" --ks-alias=Container --cn=$CONTAINER_HOSTNAME_PROPERTY --o=XSEDE --l=Nationwide --c=US --ou=GFFS /etc/ContainerGroupCertGenerator

  if [ $? -ne 0 ]; then
    echo "Failed to generate a certificate in: $file"
    echo "There may be more information in: ~/.GenesisII/grid-client.log"
    echo "and in the grid's root container's log file."
    exit 1
  fi
}

##############

function complain_re_missing_deployment_variable()
{
  echo 
  echo "There was a problem finding a variable in the deployment."
  echo "It is expected to be present in:"
  echo "  $file"
  echo "Under an entry called:"
  echo "  $var"
  echo
  exit 1
}

##############

function dump_important_variables()
{
  echo "hostname: '$CONTAINER_HOSTNAME_PROPERTY'"
  echo "port: '$CONTAINER_PORT_PROPERTY'"
  echo "tls cert file: '$TLS_KEYSTORE_FILE_PROPERTY'"
  echo "tls key pass: '$TLS_KEY_PASSWORD_PROPERTY'"
  echo "tls keystore pass: '$TLS_KEYSTORE_PASSWORD_PROPERTY'"
  echo "signing cert file: '$SIGNING_KEYSTORE_FILE_PROPERTY'"
  echo "signing key pass: '$SIGNING_KEY_PASSWORD_PROPERTY'"
  echo "signing keystore pass: '$SIGNING_KEYSTORE_PASSWORD_PROPERTY'"
  echo "signing key alias: '$SIGNING_KEY_ALIAS_PROPERTY'"
}

##############

