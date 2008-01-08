#!/bin/sh

if [ $# -lt 1 ]
then
	echo "USAGE:  shim-%{OGRSH_ARCH}.sh <program-to-shim> [args]"
	exit 1
fi

JSERVER_LOCATION="%{INSTALL_PATH}"

TMP_FILENAME=/tmp/$USER.shim.$RANDOM
while [ -e $TMP_FILENAME ]
do
	TMP_FILENAME=/tmp/$USER.shim.$RANDOM
done

cd "$JSERVER_LOCATION"
"./jserver.sh" > $TMP_FILENAME &
JSERVER_PID=$!

while [ ! -e $TMP_FILE ]
do
	sleep 1
done

LINES=0
while [[ $LINES -eq 0 ]]
do
	LINES=`wc -l $TMP_FILENAME | sed -e "s/ .*//" 2> /dev/null`
done
export LINES=

LINE=`head -1 $TMP_FILENAME`
export OGRSH_JSERVER_ADDRESS="127.0.0.1"
export OGRSH_JSERVER_SECRET=`echo $LINE | sed -e 's/^Server.//' | sed -e 's/].*//'`
export OGRSH_JSERVER_PORT=`echo $LINE | sed -e 's/^.*port //'`
export LINE=
export HOME="/home/%{USER_NAME}"
export OGRSH_CONFIG="%{INSTALL_PATH}/OGRSH/config/ogrsh-conf.xml"
export LD_LIBRARY_PATH="%{INSTALL_PATH}/OGRSH/lib/%{OGRSH_ARCH}:$LD_LIBRARY_PATH"
export LD_PRELOAD=libOGRSH.so

PROGRAM="$1"
shift
"$PROGRAM" "$@"

export LD_PRELOAD=
export OGRSH_JSERVER_SECRET=

kill $JSERVER_PID
/bin/rm -f -r $TMP_FILENAME
