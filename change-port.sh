#!/bin/bash

if [ $# -ne 3 ]
then
	echo "USAGE:  $0 <deployment-dir> <old-port> <new-port>"
	exit 1
fi

DEPLOYMENT_DIR="$1"
OLD_PORT=$2
NEW_PORT=$3

function checkfile # filename
{
	if [ ! -e "$1" ]
	then
		echo "File $1 does not exist."
		return 1
	fi

	if [ ! -r "$1" ]
	then
		echo "Unable to read file $1."
		return 1
	fi

	if [ ! -w "$1" ]
	then
		echo "Unable to write file $1."
		return 1
	fi

	return 0
}

function replacefile #filename oldport newport
{
	local tmpfile

	echo "Modifying file \"$1\"."

	checkfile "$1"
	if [ $? -ne 0 ]
	then
		return 1
	fi

	tmpfile="$1.tmp"
	cat "$1" | sed -e "s/$2/$3/g" > "$tmpfile"
	if [ $? -ne 0 ]
	then
		echo "An error occurred while creating tmp file."
		return 1
	fi

	mv "$tmpfile" "$1"
	if [ $? -ne 0 ]
	then
		echo "An error occurred while replacing the original file."
		return 1
	fi

	return 0
}

replacefile "$DEPLOYMENT_DIR/configuration/bootstrap.xml" $OLD_PORT $NEW_PORT
replacefile "$DEPLOYMENT_DIR/configuration/server-config.xml" $OLD_PORT $NEW_PORT
replacefile "$DEPLOYMENT_DIR/configuration/web-container.properties" $OLD_PORT $NEW_PORT
# heuristic for second port opened.
replacefile "$DEPLOYMENT_DIR/configuration/web-container.properties" $(expr $OLD_PORT + 1) $(expr $NEW_PORT + 1)
