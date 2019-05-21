#!/bin/bash

# attempts to use the login nonce features to create a login for the user and password provided on the
# command line and then to run some test methods on that user.  will run until interrupted.

port="$1"; shift
user="$1"; shift
password="$1"; shift

if [ -z "$port" -o -z "$user" -o -z "$password" ]; then
  echo "Usage: $(basename $0) port username password"
  echo
  echo This script requires a port, user, and password on the command line.
  echo Given those paramters, it will create a login nonce using a running
  echo clientserver command listening on the port and start feeding it
  echo commands.  It will randomly sleep between commands.
  exit 1
fi

nonce_string="$(echo "login --username=$user --password=$password --create-nonce" | netcat localhost $port)"
if [ $? -ne 0 ]; then
  echo nonce creation failed.  response was:
  echo $nonce_string
  exit 1
fi

echo got response and need to parse this:
echo $nonce_string

# grab out just our nonce report line.
nonce=$(echo $nonce_string | sed -e 's/^#nonce=\([^# ]*\) #.*$/\1/')

echo just the nonce appears to be: $nonce

while true; do
  # snooze a random time period, up to 5 seconds.
  sleep $(expr $RANDOM % 5 + 1)

  echo -- whoami
  echo context $nonce whoami | netcat localhost $port
  echo -- pwd
  echo context $nonce pwd | netcat localhost $port
  echo -- ls
  echo context $nonce ls | netcat localhost $port

done

