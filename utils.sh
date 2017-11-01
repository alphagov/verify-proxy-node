#!/bin/bash

NO_COLOUR='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
ORANGE='\033[0;33m'
PURPLE='\033[0;35m'
GRAY='\033[0;37m'
YELLOW='\033[1;33m'

function colour_print() {
	echo -e "$1$2${NO_COLOUR}"
}
