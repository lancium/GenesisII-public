#!/bin/bash

# Gets all the repositories that we wish to build documentation for and then
# runs doxygen on all of them.
#
# Author: Chris Koeritz

# download the source code and output the documentation into this folder.
docs_dir="$HOME/doc-arbor"
# svn repository where we will get the code from.
svn_top="svn://svn.xcg.virginia.edu:9002/GENREPO"

export WORKDIR="$( \cd "$(\dirname "$0")" && \pwd )"  # obtain the script's working directory.

function make_dir()
{
  local dir="$1"; shift
  if [ ! -d "$dir" ]; then mkdir -p "$dir"; fi
}

function get_code()
{
  dir="$1"; shift
  repo="$1"; shift
  make_dir "$dir"
  if [ $? -ne 0 ]; then echo Failed to make directory $dir; exit 1; fi
  pushd "$dir" &>/dev/null
  svn co "$repo"
  if [ $? -ne 0 ]; then echo Failed to check out repo $repo; exit 1; fi
  popd &>/dev/null
}

# now actually pull out the code trees we wish to document.
get_code "$docs_dir/genesis2" "$svn_top/GenesisII/trunk"
#get_code "$docs_dir/fsview2" "$svn_top/FSViewII/trunk"
#get_code "$docs_dir/app_mgr" "$svn_top/ApplicationManager/trunk"
#get_code "$docs_dir/gen2bes" "$svn_top/GeniiBES/trunk"
#get_code "$docs_dir/gen2jsdl" "$svn_top/GeniiJSDL/trunk"
#get_code "$docs_dir/procmgmt" "$svn_top/GeniiProcessMgmt/trunk"
#get_code "$docs_dir/jobtool" "$svn_top/GridJobTool/trunk"

# set the variable that doxygen wants to know.
export DOC_TOP="$docs_dir"

# chomp on all our docs.  this takes a while.
bash "$WORKDIR/run_doxygen.sh"

