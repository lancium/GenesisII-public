#!/bin/bash
## Author: Vanamala Venkataswamy

echo "This application will fault"
which cc
cc seg-fault.c -o seg-fault
./seg-fault
