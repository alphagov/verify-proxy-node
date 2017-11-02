#!/bin/bash -e

source ./utils.sh

function install_gradle() {
	brew update && brew install gradle
	colour_print $GREEN "Gradle installation complete!"

}

function install_cloudfoundry() {
	brew tap cloudfoundry/tap
	brew update && brew install cf-cli
	colour_print $GREEN "Cloud Foundry installation complete!"
}

colour_print $ORANGE "========================="
colour_print $GREEN "Installing Gradle"
colour_print $ORANGE "========================="
if gradle -v; then
	colour_print $GREEN "Gradle already exists! Doing nothing"
else
	install_gradle
fi

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