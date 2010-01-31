#!/bin/bash

###############################################################################
# Copyright 2010 University of Virginia
#
# Configures the Unicore site installation for use with the XCG GII grid.  Must 
# provide the "containergrp" keystore password to unlock that certificate 
# for use as the Unicore service CA.  
#
# Edit the variables below to change the XCG bootstrap installation's security 
# directory from which the XCG grid's security certs/etc. can be found.  
# 
# Author: dgm4d
# Date: 1/31/2010
#
###############################################################################

if [ $# -ne 2 ]
then
	echo "configure-for-gii.sh <unicore-server-install-dir> <container-grp-password>"
	exit
fi

GII_BOOTSTRAP_SEC="cicero.cs.virginia.edu:/localtmp/gbg/GeniiNet2_0Install/deployments/GeniiNetBootstrapContainer/security"
CERT_TOOL="/localtmp/gbg/GeniiNet2_0Install/cert-tool"

UNICORE=$(readlink -f "$1")
VSITE=`grep "uas.targetsystem.sitename" $UNICORE/unicorex/conf/uas.config | sed 's/uas.targetsystem.sitename=//g'`

# Trust store for all of XCG (in jks format)
TRUSTED_PFX="$UNICORE/certs/trusted.jks"
TRUSTED_PASS="trusted"

# Trust store for VSITE services behind the gateway
VSITE_TRUSTED_PFX="$UNICORE/certs/vsite-trusted.jks"
VSITE_TRUSTED_PASS="trusted"

# CA for the VSITE services
VSITE_CA="containergrp"
VSITE_CA_CER="$UNICORE/certs/$VSITE_CA.cer"
VSITE_CA_PFX="$UNICORE/certs/$VSITE_CA.pfx"
VSITE_CA_PASS="$2"
VSITE_CA_ALIAS="GenesisII Container Group CA Cert"




# Copy container CA from bootstrap install
echo
echo Copy container CA from bootstrap install
scp "$GII_BOOTSTRAP_SEC/$VSITE_CA*" "$UNICORE/certs"


# Copy trusted certs from bootstrap install
echo
echo Copy trusted certs from bootstrap install
scp "$GII_BOOTSTRAP_SEC/*.cer" "$UNICORE/certs"
scp -r "$GII_BOOTSTRAP_SEC/certs/trusted" "$UNICORE/certs"


# Create gateway trust store from trusted certs (Because Unicore doesn't do PKCS12 truststores)
echo
echo Create gateway trust store from trusted certs
for i in `ls $UNICORE/certs/trusted/*.cer`
do
	ALIAS=`echo "$i" | sed "s'.cer''g" | sed "s'.*/''g"`
	keytool -importcert -noprompt -trustcacerts -alias $ALIAS -file "$i" -keystore $TRUSTED_PFX -storepass $TRUSTED_PASS
done


# Create VSITE container trust store containing vsite and admin
echo
echo Create VSITE container trust store containing only the vsite ca and admin
keytool -importcert -noprompt -trustcacerts -alias "$VSITE" -file $VSITE_CA_CER -keystore $VSITE_TRUSTED_PFX -storepass $VSITE_TRUSTED_PASS
keytool -importcert -noprompt -trustcacerts -alias "VCGR Admin" -file $UNICORE/certs/admin.cer -keystore $VSITE_TRUSTED_PFX -storepass $VSITE_TRUSTED_PASS


# Generate Unicore service container certs
echo
echo Generate Unicore service container certs
for SERVICE in gateway registry unicorex xuudb
do
	if [ -d $UNICORE/$SERVICE ]
	then 
		CN="Unicore $VSITE $SERVICE (`hostname`)"
		DN="C=US, ST=Virginia, L=Charlottesville, O=UVA, OU=VCGR, CN=$CN"
		SERVICE_PFX="$UNICORE/certs/$SERVICE.p12"
		SERVICE_PASS=$SERVICE

		echo "Creating Service Certificate for $SERVICE"
		$CERT_TOOL gen -keysize=1024 "-dn=$DN" "-input-keystore=$VSITE_CA_PFX" "-input-keystore-pass=$VSITE_CA_PASS" "-input-alias=$VSITE_CA_ALIAS" "-output-keystore=$SERVICE_PFX" "-output-keystore-pass=$SERVICE_PASS" "-output-alias=$SERVICE"
		keytool -exportcert -alias $SERVICE -file $UNICORE/certs/$SERVICE.cer -keystore $SERVICE_PFX -storepass $SERVICE_PASS -storetype PKCS12
		keytool -importcert -noprompt -trustcacerts -alias $SERVICE -file $UNICORE/certs/$SERVICE.cer -keystore $VSITE_TRUSTED_PFX -storepass $VSITE_TRUSTED_PASS

		echo
	fi
done


if [ -d $UNICORE/gateway ]
then
	# Configure gateway security (trust everything in the GII truststore)
	echo
	echo Configure gateway security
	sed -i "s'keystore=.*'keystore=$UNICORE/certs/gateway.p12'g" $UNICORE/gateway/conf/security.properties
	sed -i "s'keystorepassword=.*'keystorepassword=gateway'g" $UNICORE/gateway/conf/security.properties
	sed -i "s'truststore=.*'truststore=$TRUSTED_PFX'g" $UNICORE/gateway/conf/security.properties
	sed -i "s'truststorepassword=.*'truststorepassword=$TRUSTED_PASS'g" $UNICORE/gateway/conf/security.properties

	# require gateway assertions to be signed
	sed -i "s'signConsignorToken = .*'signConsignorToken = true'g" $UNICORE/gateway/conf/gateway.properties

	# change to listen on all interfaces (obviate problems where not visible off-machine)
	sed -i "s'hostname =.*'hostname = https://0.0.0.0:8080'g" $UNICORE/gateway/conf/gateway.properties
fi


if [ -d $UNICORE/unicorex ]
then
	# Configure unicorex security (only trust other containers)
	echo
	echo Configure unicorex security
	sed -i "/keystore.jks/d" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "/keyalias/d" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keystore\".*'unicore.wsrflite.ssl.keystore\" value=\"$UNICORE/certs/unicorex.p12\"/>'g" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keypass\".*'unicore.wsrflite.ssl.keypass\" value=\"unicorex\"/>'g" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keytype\".*'unicore.wsrflite.ssl.keytype\" value=\"PKCS12\"/>'g" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.truststore\".*'unicore.wsrflite.ssl.truststore\" value=\"$VSITE_TRUSTED_PFX\"/>'g" $UNICORE/unicorex/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.truststorepass\".*'unicore.wsrflite.ssl.truststorepass\" value=\"$VSITE_TRUSTED_PASS\"/>'g" $UNICORE/unicorex/conf/wsrflite.xml

	# Require signatures from gateway assertions to be checked
	sed -i "s'uas.security.consignor.checksignature=.*'uas.security.consignor.checksignature=true'g" $UNICORE/unicorex/conf/uas.config

	# Do not require message-level signatures (we authn at the gateway, and GII doesn't sign messages by default)
	sed -i "s'uas.security.signatures=.*'uas.security.signatures=false'g" $UNICORE/unicorex/conf/uas.config

	# Configure unicorex for BES
	echo
	echo Configure unicorex for BES
	sed -i "s'uas.onstartup='uas.onstartup=de.fzj.unicore.bes.util.BESOnStartup 'g" $UNICORE/unicorex/conf/uas.config

	# Configure UCC security (to use unicorex UAS cred)
	echo
	echo Configure UCC security
	sed -i "s'keystore=.*'keystore=$UNICORE/certs/unicorex.p12'g" $UNICORE/unicorex/conf/ucc.preferences
	sed -i "s'storetype=.*'storetype=PKCS12'g" $UNICORE/unicorex/conf/ucc.preferences
	sed -i "s'password=.*'password=unicorex'g" $UNICORE/unicorex/conf/ucc.preferences
	sed -i "s'alias=.*'alias=unicorex'g" $UNICORE/unicorex/conf/ucc.preferences
	echo truststore=$VSITE_TRUSTED_PFX >> $UNICORE/unicorex/conf/ucc.preferences
	echo truststorePassword=$VSITE_TRUSTED_PASS >> $UNICORE/unicorex/conf/ucc.preferences
fi


if [ -d $UNICORE/xuudb ]
then
	# Configure xuudb service security
	echo
	echo Configure xuudb service security
	sed -i "s'.*xuudb_keystore_file=.*'xuudb_keystore_file=$UNICORE/certs/xuudb.p12'g" $UNICORE/xuudb/conf/xuudb_server.conf
	sed -i "s'.*xuudb_keystore_password=.*'xuudb_keystore_password=xuudb'g" $UNICORE/xuudb/conf/xuudb_server.conf
	sed -i "s'.*xuudb_keystore_type=.*'xuudb_keystore_type=PKCS12'g" $UNICORE/xuudb/conf/xuudb_server.conf
	sed -i "s'.*xuudb_truststore_file=.*'xuudb_truststore_file=$VSITE_TRUSTED_PFX'g" $UNICORE/xuudb/conf/xuudb_server.conf
	sed -i "s'.*xuudb_truststore_password=.*'xuudb_truststore_password=$VSITE_TRUSTED_PASS'g" $UNICORE/xuudb/conf/xuudb_server.conf

	# Configure xuudb client security
	echo
	echo Configure xuudb client security
	sed -i "s'.*xuudb_keystore_file=.*'xuudb_keystore_file=$UNICORE/certs/xuudb.p12'g" $UNICORE/xuudb/conf/xuudb_client.conf
	sed -i "s'.*xuudb_keystore_password=.*'xuudb_keystore_password=xuudb'g" $UNICORE/xuudb/conf/xuudb_client.conf
	sed -i "s'.*xuudb_truststore_file=.*'xuudb_truststore_file=$VSITE_TRUSTED_PFX'g" $UNICORE/xuudb/conf/xuudb_client.conf
	sed -i "s'.*xuudb_truststore_password=.*'xuudb_truststore_password=$VSITE_TRUSTED_PASS'g" $UNICORE/xuudb/conf/xuudb_client.conf
	sed -i "/xuudb_keystore_type/d" $UNICORE/xuudb/conf/xuudb_client.conf
	echo xuudb_keystore_type=PKCS12 >> $UNICORE/xuudb/conf/xuudb_client.conf
fi


if [ -d $UNICORE/registry ]
then

	# Configure registry security
	echo
	echo Configure registry security
	sed -i "/keystore.jks/d" $UNICORE/registry/conf/wsrflite.xml
	sed -i "/keyalias/d" $UNICORE/registry/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keystore\".*'unicore.wsrflite.ssl.keystore\" value=\"$UNICORE/certs/registry.p12\"/>'g" $UNICORE/registry/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keypass\".*'unicore.wsrflite.ssl.keypass\" value=\"registry\"/>'g" $UNICORE/registry/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.keytype\".*'unicore.wsrflite.ssl.keytype\" value=\"PKCS12\"/>'g" $UNICORE/registry/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.truststore\".*'unicore.wsrflite.ssl.truststore\" value=\"$VSITE_TRUSTED_PFX\"/>'g" $UNICORE/registry/conf/wsrflite.xml
	sed -i "s'unicore.wsrflite.ssl.truststorepass\".*'unicore.wsrflite.ssl.truststorepass\" value=\"$VSITE_TRUSTED_PASS\"/>'g" $UNICORE/registry/conf/wsrflite.xml
fi

echo
echo Done.
