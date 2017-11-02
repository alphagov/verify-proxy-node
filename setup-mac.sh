#!/usr/bin/env bash

set -e

RED="$(tput setaf 1)"
GREEN="$(tput setaf 2)"
ORANGE="$(tput bold; tput setaf 1)"
PURPLE="$(tput setaf 5)"
GRAY="$(tput setaf 7)"
YELLOW="$(tput setaf 3)"

function colour_print() {
	echo -e "$1$2$(tput sgr0)"
}

function install_cloudfoundry() {
	brew tap cloudfoundry/tap
	brew update && brew install cf-cli
	colour_print $GREEN "Cloud Foundry installation complete!"
}

colour_print $ORANGE "========================="
colour_print $GREEN "Installing Cloud Foundry"
colour_print $ORANGE "========================="
if cf -v; then
	colour_print $GREEN "Cloud Foundry already exists! Doing nothing"
else
	install_cloudfoundry
fi

colour_print $ORANGE "========================="
colour_print $GREEN "Setup Complete!!"
colour_print $ORANGE "========================="
