#!/bin/bash

ipaddress=$(ifconfig | grep "inet addr" | grep -v 127.0.0.1 | sed -e 's/ *inet addr:\([^ ][^ ]*\) .*/\1/')

newDIR=/home/xcgmain/GenesisIIShell

cd $newDIR 

sleep 60

./grid login --username=uvaresources --password=password

./grid attach-host https://$ipaddress:18443/axis/services/VCGRContainerPortType /uninitialized-containers/uvaresources/$ipaddress

./grid create-resource /uninitialized-containers/uvaresources/$ipaddress/Services/GeniiBESPortType /home/uvaresources/BES/$ipaddress

./grid ln /home/uvaresources/BES/$ipaddress /home/uvaresources/uvaresource-queue/resources/$ipaddress

./grid logout --all

wget http://www.cs.virginia.edu/~vcgr/xcg3install/restart.sh

chmod +x restart.sh

bash restart.sh


