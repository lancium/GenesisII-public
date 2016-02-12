#!/bin/bash

# this file is created with the cleaned up set of gffs operations found in the logs.
output_file=$HOME/accumulated_operations.txt
rm -f $output_file

# modify this when looking for a different year.
YEAR=2015

# get the directory from command line.
directory="$1"; shift

if [ -z "$directory" ]; then
  echo This script needs a directory where a bunch of log files reside.
  echo It will spider through all the files and find invocations of gffs
  echo operations.  All of the recorded ops will be reported in a file
  echo called:
  echo -e "\t$output_file"
  exit 1
fi

tempaccum=$HOME/accumulated_user_list.temp
rm -f $tempaccum

junkfile=$HOME/log_pile.temp
rm -f $junkfile

find "$directory" -iname "stats.log*" -type f -exec cat {} >>$junkfile ';'

# look at the big file and grab out the user auth events.
grep "^$YEAR-.*EnhancedRNSPortType: " $junkfile | sed -n -e "s/^\($YEAR-..-..\).*EnhancedRNSPortType: \(.*\) from \(.*\)$/\1 \3 rns:\2/p" >>$tempaccum
grep "^$YEAR-.*RandomByteIO: " $junkfile | sed -n -e "s/^\($YEAR-..-..\).*RandomByteIO: \(.*\) from \(.*\)$/\1 \3 rbyteio:\2/p" >>$tempaccum
grep "^$YEAR-.*GeniiResolver: " $junkfile | sed -n -e "s/^\($YEAR-..-..\).*GeniiResolver: \(.*\) from \(.*\)$/\1 \3 resolve:\2/p" >>$tempaccum

sort $tempaccum | uniq >$output_file
rm $tempaccum
rm $junkfile

