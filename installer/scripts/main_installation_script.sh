#!/bin/bash

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
  if [ -z "$file" -o -z "$phrase" -o -z "$replacement" ]; then
    echo "replace_phrase_in_file: needs a filename, a phrase to replace, and the"
    echo "text to replace that phrase with."
    return 1
  fi
  sed -i -e "s%$phrase%$replacement%g" "$file"
}

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

  for fname in $(find $dir -type f -exec grep -l installer:sys.installationDir {} ';'); do
    local seeking="\${installer:sys.installationDir}"
    local replacement="$dir"
    replace_phrase_in_file "$fname" "$seeking" "$replacement"
  done
}

##############

# bootstrap our information about the installation, starting with where it
# resides.
GENII_INSTALL_DIR="$1"; shift

replace_compiler_variables $GENII_INSTALL_DIR/RELEASE
replace_compiler_variables $GENII_INSTALL_DIR/updates/current-version.txt
replace_compiler_variables $GENII_INSTALL_DIR/container.properties

replace_installdir_variables $GENII_INSTALL_DIR

# make a link for the Container startup script.
ln -s $GENII_INSTALL_DIR/JavaServiceWrapper/wrapper/bin/GFFSContainer $GENII_INSTALL_DIR

# clean up some older files.
\rm -rf "$GENII_INSTALL_DIR/ApplicationWatcher" "$GENII_INSTALL_DIR/XCGContainer" "$GENII_INSTALL_DIR/lib/gffs-container.jar" 

##############

echo "Finished preparing installation for GenesisII GFFS."
exit 0


