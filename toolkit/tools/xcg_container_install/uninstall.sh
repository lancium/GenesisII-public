#!/bin/bash

wget http://www.cs.virginia.edu/~vcgr/xcg3install/unlink.sh

bash unlink.sh ~xcgmain

cd /home

rm -rf xcgmain

rm -rf xcgjob

deluser xcgmain

deluser xcgjob
