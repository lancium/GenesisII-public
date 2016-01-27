#!/bin/bash
command -v mpicc &>/dev/null || { simple-mpi-cray; exit $?; }
simple-mpi; exit $?
