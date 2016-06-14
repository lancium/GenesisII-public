#!/bin/bash

function date_stringer() {
  local sep="$1"; shift
  if [ -z "$sep" ]; then sep='_'; fi
  date +"%Y$sep%m$sep%d$sep%H%M$sep%S" | tr -d '/\n/'
}

zip -r "$HOME/genesis2_wiki_as_of_$(date_stringer).zip" /z/uvaweb/doc/wiki -x "*/xcg_releases/*" -x "*/xsede_beta/*" -x "*/GenesisII*gz" -x "*/act126_installers/*" -x "*/genesis2-*" -x "*/udc3_releases/*" -x "*/xsede_releases/*" -x "*/gffseu_releases/*"


