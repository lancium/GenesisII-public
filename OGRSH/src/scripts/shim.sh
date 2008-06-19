#!/bin/sh

function waitForFile() # Filepath wait-time
{
	local COUNT=0
	while [ $COUNT -lt $2 ]
	do
		if [ -r "$1" ]
		then
			return
		fi

		sleep 1
		COUNT=$(( $COUNT + 1 ))
	done

	echo "Error:  Server file didn't show up in $2 seconds."
	exit 1
}

function setServerInfo() # filepath wait-time
{
	local COUNT=0
	local LINE=""

	while [ $COUNT -lt $2 ]
	do
		LINE=`cat "$1" | grep "listening on port" 2> /dev/null`
		if [ $? -eq 0 ]
		then
			export OGRSH_JSERVER_SECRET=`echo $LINE | sed -e 's/^Server.//' | sed -e 's/].*//'`
			export OGRSH_JSERVER_PORT=`echo $LINE | sed -e 's/^.*port //'`
			return
		fi

		sleep 1
		COUNT=$(( $COUNT + 1 ))
	done

	echo "Error:  Server file didn't exhibit the format expected."
	exit 1
}

# Some Constants
GENII_CRED_URI_PARM="--genii-credential-uri="
GENII_CRED_USER_PARM="--genii-credential-user="
GENII_CRED_PASS_PARM="--genii-credential-pass="
GENII_CRED_PATTERN_PARM="--genii-credential-pattern="

if [ $# -lt 1 ]
then
	echo "USAGE:  shim-%{OGRSH_ARCH}.sh [options] <program-to-shim> [args]"
	echo "	WHERE options are:"
	echo "		$GENII_CRED_URI_PARM<credential-uri>"
	echo "		$GENII_CRED_USER_PARM<credential-user>"
	echo "		$GENII_CRED_PASS_PARM<credential-password>"
	echo "		$GENII_CRED_PATTERN_PARM<credential-pattern>"
	exit 1
fi

GENII_CREDENTIAL_URI=
GENII_CREDENTIAL_USER=
GENII_CREDENTIAL_PASS=
GENII_CREDENTIAL_PATTERN=

DONE=false
while [ $DONE != true ]
do
	if [ X"$1" = X ]
	then
		echo "USAGE:  shim-%{OGRSH_ARCH}.sh [options] <program-to-shim> [args]"
		echo "	WHERE options are:"
		echo "		$GENII_CRED_URI_PARM<credential-uri>"
		echo "		$GENII_CRED_USER_PARM<crednetial-user>"
		echo "		$GENII_CRED_PASS_PARM<credential-password>"
		echo "		$GENII_CRED_PATTERN_PARM<credential-pattern>"
		exit 1
	fi

	case "$1" in
		$GENII_CRED_URI_PARM*)
			export GENII_CREDENTIAL_URI="${1:${#GENII_CRED_URI_PARM}}"
			shift
			;;
		$GENII_CRED_USER_PARM*)
			export GENII_CREDENTIAL_USER="${1:${#GENII_CRED_USER_PARM}}"
			shift
			;;
		$GENII_CRED_PASS_PARM*)
			export GENII_CREDENTIAL_PASS="${1:${#GENII_CRED_PASS_PARM}}"
			shift
			;;
		$GENII_CRED_PATTERN_PARM*)
			export GENII_CREDENTIAL_PATTERN="${1:${#GENII_CRED_PATTERN_PARM}}"
			shift
			;;
		*)
			DONE=true
			;;
	esac
done

JSERVER_LOCATION="%{INSTALL_PATH}"

TMP_FILENAME=/tmp/$USER.shim.$RANDOM
while [ -e $TMP_FILENAME ]
do
	TMP_FILENAME=/tmp/$USER.shim.$RANDOM
done

cd "$JSERVER_LOCATION"
"./jserver.sh" > $TMP_FILENAME &
JSERVER_PID=$!

waitForFile "$TMP_FILENAME" 5
setServerInfo "$TMP_FILENAME" 5
export OGRSH_JSERVER_ADDRESS="127.0.0.1"

if [ -n "$BES_HOME" ]
then
	export HOME="$BES_HOME"
else
	export HOME="/home/%{USER_NAME}"
fi

if [ -z "$OGRSH_CONFIG" ]
then
	export OGRSH_CONFIG="%{INSTALL_PATH}/OGRSH/config/ogrsh-conf.xml"
fi

export LD_LIBRARY_PATH="%{INSTALL_PATH}/OGRSH/lib/%{OGRSH_ARCH}:$LD_LIBRARY_PATH"
export LD_PRELOAD=libOGRSH.so

PROGRAM="$1"
shift
"$PROGRAM" "$@"

export LD_PRELOAD=
export OGRSH_JSERVER_SECRET=

kill $JSERVER_PID
/bin/rm -f -r $TMP_FILENAME
