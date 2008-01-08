#!/bin/sh

if [ $# -ne 3 ]
then
	echo "USAGE:  $0 <target> <variable> <value>"
	exit 1
fi

echo "Patching $1:  $2 -> $3"

export TMP_FILENAME="$1.patch"

cat "$1" | sed -e "s/$2/$3/g" > "$TMP_FILENAME"
cp "$TMP_FILENAME" "$1"
/bin/rm -f -r "$TMP_FILENAME"
