#!/bin/sh

if [ $# -lt 4 ]
then
	echo "USAGE:  debug-shim.sh <jserver-ip> <jserver-port> <jserver-secret> <program-to-shim>"
	exit 1
fi

export OGRSH_JSERVER_ADDRESS="$1"
export OGRSH_JSERVER_SECRET="$3"
export OGRSH_JSERVER_PORT="$2"
export HOME="/home/mark-morgan"
export OGRSH_CONFIG="$OGRSH_HOME/config/example.xml"
export LD_LIBRARY_PATH="$OGRSH_HOME/lib/$OGRSH_HOME:$LD_LIBRARY_PATH"
export LD_PRELOAD=libOGRSH.so

PROGRAM="$4"
shift
shift
shift
shift
"$PROGRAM" "$@"
