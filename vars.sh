#!/bin/sh

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# DEFAULT CONFIGURATION
. "${BASE_DIR}/vars-default.sh"

# LOAD LOCAL OVERRIDEN CONFIGURATION
if [ -e "${BASE_DIR}/vars-local.sh" ]; then
  . "${BASE_DIR}/vars-local.sh"
fi
