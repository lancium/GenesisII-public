#!/bin/bash

# this file is created with the cleaned up set of authentication events.
output_file=$HOME/accumulated_user_list.txt
rm -f $output_file

# modify this when looking for a different year.
YEAR=2015

# get the directory from command line.
directory="$1"; shift

if [ -z "$directory" ]; then
  echo This script needs a directory where a bunch of log files reside.
  echo It will spider through all the files and find authentication events.
  echo All authentication events will be reported in a file called:
  echo -e "\t$output_file"
  exit 1
fi

tempaccum=$HOME/accumulated_user_list.temp
rm -f $tempaccum

junkfile=$HOME/log_pile.temp
rm -f $junkfile

find "$directory" -type f -exec cat {} >>$junkfile ';'

# look at the big file and grab out the user auth events.
grep "^$YEAR-.*authenticating user" $junkfile | sed -n -e "s/^\($YEAR-..-..\).*user '\([^']*\)'.*'.*'.*$/\1 \2/p" >$tempaccum

sort $tempaccum | uniq >$output_file
rm $tempaccum
rm $junkfile

