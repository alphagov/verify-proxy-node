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

function install_pre_commit() {
	brew update && brew install pre-commit
  pre-commit install
	colour_print $GREEN "pre-commit installation complete!"
}

colour_print $ORANGE "========================="
colour_print $YELLOW "Installing Cloud Foundry"
if command -v cf >/dev/null; then
	colour_print $GRAY "Cloud Foundry already exists! Doing nothing"
else
	install_cloudfoundry
fi
echo

colour_print $ORANGE "========================="
colour_print $YELLOW "Installing pre-commit"
if command -v pre-commit >/dev/null; then
	colour_print $GRAY "pre-commit already exists! Doing nothing"
else
	install_pre_commit
fi
echo

colour_print $ORANGE "========================="
colour_print $YELLOW "Installing adr-tools"
if brew ls --versions adr-tools >/dev/null; then
	colour_print $GRAY "adr-tools already exists! Doing nothing"
else
	brew update && brew install adr-tools
	colour_print $GREEN "adr-tools installation complete!"
fi
echo

colour_print $ORANGE "========================="
colour_print $YELLOW "Installing xmlsectool"
if brew ls --versions xmlsectool >/dev/null; then
	colour_print $GRAY "xmlsectool already exists! Doing nothing"
else
	brew update && brew install xmlsectool
	colour_print $GREEN "adr-tools installation complete!"
fi
echo

colour_print $GREEN "Setup Complete!!"
