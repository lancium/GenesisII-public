#!/bin/sh
#PBS -N out-write 

#PBS -j oe
#PBS -l select=20:ncpus=1:mem=2gb

cd "$PBS_O_WORKDIR"
mpiexec ./writetest $RNSPATH
