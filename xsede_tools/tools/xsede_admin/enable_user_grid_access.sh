#!/bin/bash

# this assumes that the users and homes are still mainly in /users/xsede.org.full
# and /home/xsede.org.full, and that the user already exists there.  this will
# link their user and home folder into the default locations and add them to the
# gffs-users group.
#
# NOTE: the addition to gffs-users group should be removed once sdiact-149 is
# deployed on the production containers.

username="$1"; shift
if [ -z "$username" ]; then
  echo "This script needs a username to enable grid access for.  This should be the"
  echo "short form of the name, without any paths."
  exit 1
fi

echo "linking full version of user's STS into normal /users/xsede.org..."
grid ln /users/xsede.org.full/$username /users/xsede.org/$username
if [ $? -ne 0 ]; then echo "previous step failed!"; exit 1; fi
echo "linking full version of user's home into normal /home/xsede.org..."
grid ln /home/xsede.org.full/$username /home/xsede.org/$username
if [ $? -ne 0 ]; then echo "previous step failed!"; exit 1; fi
echo "giving user membership in gffs-users group..."
grid chmod /groups/xsede.org/gffs-users +rx /users/xsede.org/$username
if [ $? -ne 0 ]; then echo "previous step failed!"; exit 1; fi
echo "linking gffs-users group under user's STS for automatic login..."
grid ln /groups/xsede.org/gffs-users /users/xsede.org/$username/gffs-users
if [ $? -ne 0 ]; then echo "previous step failed!"; exit 1; fi
echo "finished enabling the user for normal grid access successfully."


