#!/bin/bash

function toMicroSeconds() # <time-value-string>
{
	local MINUTES
	local SECONDS
	local RET

	MINUTES="${1/m*/}"
	SECONDS="${1/*m/}"
	SECONDS="${SECONDS/s*/}"

	RET=`echo "($MINUTES * 60 + $SECONDS)* 1000 * 1000" | bc -q`
	echo "${RET/[^0-9]*/}"
}

function createUsageFile() # <input-output-file> <exitcode>
{
	local REALTIME
	local USERTIME
	local SYSTIME
	local LABEL
	local VALUE

	while read LABEL VALUE
	do
		case "$LABEL" in
			real)
				REALTIME=`toMicroSeconds "$VALUE"`
				;;
			user)
				USERTIME=`toMicroSeconds "$VALUE"`
				;;
			sys)
				SYSTIME=`toMicroSeconds "$VALUE"`
				;;
		esac
	done < "$1"

	echo "<exit-results exit-code=\"$2\">" > "$1"
	echo "	<user-time value=\"$USERTIME\" units=\"MICROSECONDS\"/>" >> "$1"
	echo "	<system-time value=\"$SYSTIME\" units=\"MICROSECONDS\"/>" >> "$1"
	echo "	<wallclock-time value=\"$REALTIME\" units=\"MICROSECONDS\"/>" >> "$1"
	echo "	<maximum-rss>0</maximum-rss>" >> "$1"
	echo "</exit-results>" >> "$1"
}

USAGE_FILENAME=
STDIN_REDIRECT=
STDOUT_REDIRECT=
STDERR_REDIRECT=
CMDLINE=

FINISHED_PARSING=false
while [ $FINISHED_PARSING != true ]
do
	case "$1" in
		-D*)
#			PWRAPPER_VARIABLE="${1/=*/}"
#			PWRAPPER_VALUE="${1/*=/}"
			export "${1:2}"
			;;
		-U*)
			USAGE_FILENAME="${1:2}"
			;;
		-d*)
			cd "${1:2}"
			;;
		-i*)
			STDIN_REDIRECT="${1:2}"
			;;
		-o*)
			STDOUT_REDIRECT="${1:2}"
			;;
		-e*)
			STDERR_REDIRECT="${1:2}"
			;;
		*)
			FINISHED_PARSING=true
			;;
	esac

	if [ $FINISHED_PARSING = false ]
	then
		shift
	fi
done

if [ $# -le 0 ]
then
	echo "Invalid usage:  Missing command line!" >&2
	exit 1
fi

LCV=0
while [ $# -gt 0 ]
do
	CMDLINE[$LCV]="$1"
	shift
	LCV=$(( $LCV + 1 ))
done

if [ -n "$STDIN_REDIRECT" ]
then
	CMDLINE[$LCV]="<"
	LCV=$(( $LCV + 1 ))
	CMDLINE[$LCV]="$STDIN_REDIRECT"
	LCV=$(( $LCV + 1 ))
fi

if [ -n "$STDOUT_REDIRECT" ]
then
	CMDLINE[$LCV]=">"
	LCV=$(( $LCV + 1 ))
	CMDLINE[$LCV]="$STDOUT_REDIRECT"
	LCV=$(( $LCV + 1 ))
fi

if [ -n "$STDERR_REDIRECT" ]
then
	CMDLINE[$LCV]="2>"
	LCV=$(( $LCV + 1 ))
	CMDLINE[$LCV]="$STDERR_REDIRECT"
	LCV=$(( $LCV + 1 ))
fi

if [ -n "$USAGE_FILENAME" ]
then
	eval "(" time "${CMDLINE[@]}" ")" "2>" "$USAGE_FILENAME"
	EXITCODE=$?
	createUsageFile "$USAGE_FILENAME" $EXITCODE
else
	eval "${CMDLINE[@]}"
	EXITCODE=$?
fi

exit $EXITCODE
