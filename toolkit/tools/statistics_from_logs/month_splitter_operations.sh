#!/bin/bash

# this script processes the accumulated user authentication list to find total monthly hits and unique users per month.

infile="$1"; shift

YEAR=2015

if [ -z "$infile" ]; then
  echo This script requires a statistics summary file with the format:
  echo "  <timestamp> <data...>"
  echo The script will extract statistics from that file and produce a report of items per month.
  exit 1
fi

echo Report for GFFS operations in $YEAR:
for month in 01 02 03 04 05 06 07 08 09 10 11 12; do
  echo -n "$YEAR month $month: total ips="
  echo -n $(grep "^$YEAR-$month-.. " $infile | wc -l)
  echo -n " unique ips="
  grep "^$YEAR-$month-.. " $infile | sed -n -e "s/^$YEAR-$month-.. \([0-9.]*\) .*$/\1/p" | sort | uniq | wc -l
done


