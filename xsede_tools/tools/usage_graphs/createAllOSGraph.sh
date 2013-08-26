#!/bin/bash
ploticus_path="/home/vcgr/Ploticus";
pl="$ploticus_path/src/pl"

export PLOTICUS_PREFABS="$ploticus_path/prefabs/";

name1=$1
name2=$2
name3=$3
color1=$4
color2=$5
color3=$6
inputfile=$7
xlbl=$8
ylbl=$9
shift
outfile=$9


echo "Inputfile: $inputfile"
echo "name1: $name1"
echo "Outfile: $outfile"

#$pl -prefab stack data="$inputfile" x=1 y=2 y2=3 y3=4 name="$name1" name2="$name2" name3="$name3" color=$color1 color2=$color2 color3=$color3 xlbl="$xlbl" ylbl="$ylbl" stubvert=yes xlbldistance=1 ylbldistance=.75 rectangle='1 1 12 4' autow=yes delim=comma -png -o "$outfile"
$pl -prefab stack data="$inputfile" x=1 y=2 y2=3 y3=4 name="$name1" name2="$name2" name3="$name3" color=$color1 color2=$color2 color3=$color3 xlbl="$xlbl" ylbl="$ylbl" stubvert=yes xlbldistance=1 ylbldistance=.75 delim=comma -png -o "$outfile"
