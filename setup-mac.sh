#!/bin/bash

source ./utils.sh

function install_maven() {
	colour_print $ORANGE "========================="
	colour_print $GREEN "Downloading Maven binary"
	colour_print $ORANGE "========================="
	curl http://mirror.vorboss.net/apache/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz --output apache-maven-3.5.2-bin.tar.gz

	colour_print $ORANGE "========================="
	colour_print $GREEN "Extracting Maven tar to /opt \n${YELLOW}Will require sudo password"
	colour_print $ORANGE "========================="
	sudo tar -xvf apache-maven-3.5.2-bin.tar.gz -C /opt
	
	colour_print $GREEN "Extraction Complete. Removing downloaded file..."
	rm -rf apache-maven-3.5.2-bin.tar.gz
}

function install_cloudfoundry() {
	brew tap cloudfoundry/tap
	brew install cf-cli
	colour_print $GREEN "Cloud Foundry installation complete!"
}

colour_print $ORANGE "========================="
colour_print $GREEN "Checking Maven version"
colour_print $ORANGE "========================="
if mvn -v; then
	colour_print $GREEN "Maven already exists! Doing nothing"
else
	install_maven
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
colour_print $GREEN "Setup Complete!! Here's a checklist for post setup steps:"
colour_print $GREEN "    * If you can't run mvn, run the following lines to add maven to your class path"
colour_print $YELLOW "          echo 'PATH=/opt/apache-maven-3.5.2/bin:\$PATH' >> ~/.bash_profile" 
colour_print $YELLOW "          source ~/.bash_profile" 
colour_print $ORANGE "========================="