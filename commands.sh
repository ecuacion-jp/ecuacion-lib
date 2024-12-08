#!/bin/bash

# NO NEED TO OVERWRITE ANYTHING. DON'T TOUCH!!!
# for PROJECTS repository!

. ./params.sh
. ../ecuacion-internal-lib/ecuacion-internal-lib-tools/scripts/common.sh $1

export command=$1
abendIfVarIsEmpty "$command" "1st arg(command) is required. exitting..."

export script_root_dir=../ecuacion-internal-lib/ecuacion-internal-lib-tools/scripts/projects-repository-commands
if [ ! -f $script_path ]; then
  abendWithMessage "ecuacion-lib-tools project not placed properly."
fi

export command_file=${command/-/\/}.sh
export script_path=${script_root_dir}/${command_file}
if [ ! -f $script_path ]; then
  abendWithMessage "${command} not found. path: ${script_path}"
fi

${script_path} ${@:2}
