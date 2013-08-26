#!/bin/bash
ploticus_path="/home/vcgr/Ploticus";
pl="$ploticus_path/src/pl"

name=$1
color=$2
inputfile=$3
xlbl=$4
ylbl=$5
outfile=$6

export PLOTICUS_PREFABS="$ploticus_path/prefabs/";

echo $name
echo $color
echo $inputfile
echo $xlbl
echo $ylbl
echo $outfile
echo $stdoutfile
echo $stderrfile

#ploticus command is sent as parameter
#$pl -prefab stack data="$inputfile" x=1 y=2 name="$name" color=$color xlbl="$xlbl" ylbl="$ylbl" stubvert=yes xlbldistance=1 ylbldistance=.75 rectangle='1 1 11 4' autow=yes delim=comma -png -o "$outfile"
$pl -prefab stack data="$inputfile" x=1 y=2 name="$name" color=$color xlbl="$xlbl" ylbl="$ylbl" stubvert=yes xlbldistance=1 ylbldistance=.75 delim=comma -png -o "$outfile"
