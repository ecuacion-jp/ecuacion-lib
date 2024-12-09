#!/bin/bash
#
# Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
