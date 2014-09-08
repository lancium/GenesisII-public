#!/bin/bash

# simple wrapper for backwards compatibility.

if [ -z "$1" ]; then
  WORKDIR="$( \cd "$( \dirname "$0" )" && \pwd )"
  bash "$WORKDIR/prepare_tools.sh"
else
  WORKDIR="$( \cd "$( \dirname "$1" )" && \pwd )"
  source "$WORKDIR/prepare_tools.sh" "$WORKDIR/prepare_tools.sh"
fi


