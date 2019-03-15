#!/usr/bin/env bash
#
#   Script to run the Compliance Tool test scenarios via the VSP in development mode.
#
#   Not really an EU Service as this cuts out the Proxy Node and goes straight to the VSP.  More like a non-EU mini-service in curl form.
#

# https://www.docs.verify.service.gov.uk/get-started/set-up-successful-verification-journey/#set-up-the-successful-verification-user-journey
#
# Download the package and unzip.
# cd to 'verify-service-provider-2.0.0/bin'
# run ./verify-service-provider development -u localhost:8080'
# In a real env update localhost:8080 to where the hub response should be posted to which will be the Gateway endpoint.
# Run this script...

VSP_URL="http://localhost:50300"
VSP_GENERATE_REQUEST_RESOURCE=$VSP_URL"/generate-request"
VSP_TRANSLATE_RESPONSE_RESOURCE=$VSP_URL"/translate-response"

function runScenario() {

    # scenarioResponseGeneratorLocation = $1
    # scenarioName = $2
    # scenarioNumber = $3
    # scenarioRequestId = $4

    echo
    echo "===" Running Scenario "===>" "$1"/"$2"/"$3"

    complianceToolScenarioResponse=$(curl "$1"/"$2"/"$3")
    echo ${complianceToolScenarioResponse} > ."$2""$3".html

    scenarioSamlResponse=$(xmllint --html --xpath "string(//html/body/form/input[@name='SAMLResponse']/@value)" ."$2""$3".html)

    curl --header "Content-Type: application/json"   --request POST   --data '{"samlResponse" : "'$scenarioSamlResponse'", "requestId" : "'$4'", "levelOfAssurance" : "LEVEL_1"}' $VSP_TRANSLATE_RESPONSE_RESOURCE
}

# Send AuthnRequest to VSP /generate-request
samlAuthnRequest=$(curl --header "Content-Type: application/json" --request POST --data '{"levelOfAssurance":"LEVEL_2"}' $VSP_GENERATE_REQUEST_RESOURCE)
samlRequest=$(jq -r  '.samlRequest' <<< "$samlAuthnRequest" )
requestId=$(jq -r  '.requestId' <<< "$samlAuthnRequest" )
ssoLocation=$(jq -r  '.ssoLocation' <<< "$samlAuthnRequest" )
echo ${samlRequest} > .samlRequestToHub.txt

# Post Saml AuthnRequest to the Compliance Tool
complianceToolResponse=$(curl -X POST --data-urlencode SAMLRequest@.samlRequestToHub.txt $ssoLocation)
responseGeneratorLocation=$(jq -r '.responseGeneratorLocation' <<< "$complianceToolResponse" )

echo ${responseGeneratorLocation}

# Run the Compliance Tool Scenarios
runScenario "$responseGeneratorLocation" "test-non-matching" "10" "$requestId"
runScenario "$responseGeneratorLocation" "test-non-matching" "11" "$requestId"
runScenario "$responseGeneratorLocation" "test-non-matching" "13" "$requestId"
runScenario "$responseGeneratorLocation" "test-non-matching" "14" "$requestId"
