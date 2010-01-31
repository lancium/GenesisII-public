#!/bin/sh

###############################################################################
# Copyright 2010 University of Virginia
#
# Retrieves the URI upon which the local Unicore BES can be reached.
#
# Unicore site must be fully configured and running.
#
# Author: dgm4d
# Date: 1/31/2010
#
###############################################################################

if [ $# -ne 1 ]
then
        echo "get-bes-uri.sh <unicore-server-install-dir>"
        exit
fi

UNICORE=$(readlink -f "$1")

HN=`hostname -f`

$UNICORE/unicorex/bin/ucc wsrf getproperties https://$HN:7777/services/Registry?res=default_registry | grep default_bes_factory | sed "s'.*\">''g" | sed "s'<.*''g"

