#!/bin/sh
# Author: Charlie Houghton
# Date: 2021 April 9
# This file is mainly to check that the overlay tool works with "large" files.
# Previously, Apache axis was failing because transfer blocks were > 16K,
# and failed to write to disk because of a permissions error.
# I chose 35 MB for the file size because we're transfering in 32 MB blocks,
# and if there was a problem with the block size this test might catch it.
#
# This is NOT a complete test of overlay. I still need to verify overlay works with non-zero
# offsets for each use case (local -> local, local -> grid, grid -> grid, grid -> local).

set -x

# Setup
rm local-local.txt
rm local-grid.txt
rm grid-grid.txt
rm grid-local.txt
rm test.file

fallocate -l 35M test.file

touch local-local.txt
touch local-grid.txt
touch grid-grid.txt
touch grid-local.txt

grid rm test.file
grid rm local-grid.txt
grid rm grid-grid.txt

grid touch local-grid.txt
grid touch grid-grid.txt

grid cp local:test.file test.file

# local -> local test
grid overlay local:test.file local:local-local.txt 0

# local -> grid test
grid overlay local:test.file local-grid.txt 0
grid cp local-grid.txt local:local-grid.txt

# grid -> grid test
grid overlay test.file grid-grid.txt 0
grid cp grid-grid.txt local:grid-grid.txt

# grid -> local test
grid overlay test.file local:grid-local.txt 0

# Verify all sizes hashes are equal, if not, overlay is busted...
du -b test.file local-local.txt local-grid.txt grid-grid.txt grid-local.txt
du -h test.file local-local.txt local-grid.txt grid-grid.txt grid-local.txt
md5sum test.file local-local.txt local-grid.txt grid-grid.txt grid-local.txt


# Cleanup 
rm local-local.txt
rm local-grid.txt
rm grid-grid.txt
rm grid-local.txt
rm test.file

grid rm test.file
grid rm local-grid.txt
grid rm grid-grid.txt
