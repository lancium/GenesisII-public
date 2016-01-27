#!/bin/bash

cd /home/xcgmain/GenesisIIShell

./GFFSContainer stop

./grid login --username=uvaresources --password=password

ipaddress=$(ifconfig | grep "inet addr" | grep -v 127.0.0.1 | sed -e 's/ *inet addr:\([^ ][^ ]*\) .*/\1/')

./grid unlink /home/uvaresources/uvaresource-queue/resources/$ipaddress

./grid unlink /home/uvaresources/BES/$ipaddress

./grid unlink /uninitialized-containers/uvaresources/$ipaddress  
