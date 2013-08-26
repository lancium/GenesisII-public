#!/bin/bash
input_file="$1"; shift
sweep_num="$1"; shift

echo input file is $input_file 
echo sweep num is $sweep_num

#echo directory where we are has:
#ls -al

echo here is the md5 hash for our data file.
md5sum $input_file 

echo now snoozing a bit
sleep 240

echo truncating file down to more chewable size.
truncate -s 3M $input_file
if [ $? -ne 0 ]; then
  echo "failed to truncate file for output; sending made-up output."
  dd if=/dev/urandom bs=1048576 count=3
  exit 0
fi

echo -e "and here is a gzipped dump of that file to cause lots of stage-out size\n(but not the full size penalty)."
gzip -c $input_file 

