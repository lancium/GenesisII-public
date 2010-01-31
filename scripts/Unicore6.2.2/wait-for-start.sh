#!/bin/sh

###############################################################################
# Copyright 2010 University of Virginia
#
# Waits for all installed Unicore services to be fully up and running.
#
# Author: dgm4d
# Date: 1/31/2010
#
###############################################################################


if [ $# -ne 1 ]
then 
	echo "wait-for-start.sh <unicore-server-install-dir>"
	exit
fi

UNICORE=$1

WaitForStartup() 
{
	SERVICE=$1

	STARTED=""
	COMPLETED=""

	until [ "$STARTED" != "" ] || [ "$COMPLETED" != "" ]
	do
		STARTED=`grep tarted $UNICORE/$SERVICE/logs/startup.log`
		if [ $? != 0 ] 
		then
			STARTED=""
		fi

		COMPLETED=`grep omplete $UNICORE/$SERVICE/logs/startup.log`
		if [ $? != 0 ] 
		then
			COMPLETED=""
		fi

		if [ "$STARTED" = "" ] && [ "$COMPLETED" = "" ]
		then
			echo
			netstat -anp | grep java
			echo
			echo "Waiting for $SERVICE startup (`date`)"
			sleep 15
		fi
	done
	echo "$SERVICE running."
}


for i in gateway registry xuudb unicorex
do
	if [ -d $UNICORE/$i ]
	then
		WaitForStartup $i
	fi
done

echo All services running.
