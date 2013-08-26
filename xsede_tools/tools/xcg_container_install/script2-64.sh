#!/bin/bash

ipaddress=$(ifconfig | grep "inet addr" | grep -v 127.0.0.1 | sed -e 's/ *inet addr:\([^ ][^ ]*\) .*/\1/')

DIR=/home/xcgmain/

FILE=installergenesis2_container_linux64_v2_6_0.bin

FILE1=xcg3_tls_cert.pfx

LOGFILE=wget.log

URL=http://genesis2.virginia.edu/wiki/uploads/Main/xcg_releases/4354/genesis2_container_linux64_v2_6_1.bin

URL1=http://www.cs.virginia.edu/~vcgr/xcg3install/xcg3_tls_cert.pfx

cd $DIR
wget $URL -O $FILE -o $LOGFILE
wget $URL1 -O $FILE1 

chmod +x $FILE


input="filename.tmp"
touch $input 
echo "o" >> $input
echo "/home/xcgmain/GenesisIIShell" >> $input
echo "1" >> $input
echo "18443" >> $input
echo "$ipaddress" >> $input
echo "uvaresources" >> $input
echo "n" >> $input
echo "/home/xcgmain/xcg3_tls_cert.pfx" >> $input
echo "container" >> $input
echo "xsede cert" >> $input
echo "y" >> $input

./$FILE -c < $input

URL2=http://www.cs.virginia.edu/~vcgr/xcg3install/linkContainer.sh

wget $URL2

bash linkContainer.sh ~xcgmain
