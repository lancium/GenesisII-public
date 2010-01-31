#!/bin/sh

###############################################################################
# Copyright 2010 University of Virginia
#
# Adds the GII Administrator (admin) identity to the specified Unicore site 
# installation.  
#
# Unicore site must be fully configured and running.
#
# Author: dgm4d
# Date: 1/31/2010
#
###############################################################################

if [ $# -ne 1 ]
then
        echo "add-admin-acl.sh <unicore-server-install-dir>"
        exit
fi

UNICORE=$(readlink -f "$1")

# convert to PEM
openssl x509 -in $UNICORE/certs/admin.cer -inform DER -outform PEM -out $UNICORE/certs/admin.pem

# get grid component id of BES/UAS service
GCID=`grep "xuudb_gcid=" $UNICORE/unicorex/conf/uas.config | sed 's/xuudb_gcid=//g'`

# add to xuudb
$UNICORE/xuudb/bin/admin.sh add $GCID $UNICORE/certs/admin.pem `whoami` admin
