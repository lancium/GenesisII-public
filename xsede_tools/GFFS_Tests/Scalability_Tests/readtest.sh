#!/bin/sh
#PBS -N out-read

#PBS -j oe
#PBS -l select=20:ncpus=1:mem=2gb

cd $PBS_O_WORKDIR
mpiexec ./readtest $RNSPATH/tar-file.tar
