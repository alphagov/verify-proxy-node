require 'yaml'
require 'uri'
require 'securerandom'

Given("the proxy node is sent a LOA {string} request") do |load_type|
  loa_url = case load_type
    when "Low"
      "/RequestLow"
    when "Substantial"
      "/RequestSubstantial"
    when "High"
      "/RequestHigh"
    else
      "/BadRequest"
  end
  visit(ENV.fetch('STUB_CONNECTOR_URL') + loa_url)
end

And('they progress through verify') do
  find('label', :text => 'I’ve used GOV.UK Verify before').click
  click_button('Continue')
  find('button', :text => 'Stub Idp Demo One').click
end

Given(/^the stub connector supplies an authn request with (.*)$/) do |issue|
  scenario_path_map = {
    "a missing signature": "/MissingSignature",
    "an invalid signature": "/InvalidSignature"
  }
  visit(ENV.fetch('STUB_CONNECTOR_URL') + scenario_path_map[issue.to_sym])
end

Given('they login to stub idp') do
  fill_in('username', with: ENV.fetch('STUB_IDP_USER'))
  fill_in('password', with: 'bar')
  click_on('SignIn')
  click_on('I Agree')
end

Given('they login to stub idp with error event {string}') do |error_button_text|
  fill_in('username', with: ENV.fetch('STUB_IDP_USER'))
  fill_in('password', with: 'bar')
  click_on(error_button_text)
end

Given("the user accesses a invalid page") do
  visit(ENV.fetch('PROXY_NODE_URL') + '/asdfasdfasfsaf')
end

Given("the user accesses the gateway response url directly") do
  visit(ENV.fetch('PROXY_NODE_URL') + '/SAML2/SSO/Response/POST')
end

Given("the user visits the Netherlands Connector Node Stub Page") do
  visit('https://demo-portal.minez.nl/demoportal/etoegang')
end

Given("the user visits a government service") do
  visit('https://www.gov.uk/personal-tax-account/sign-in/prove-identity')
end

And('they choose sign in with a digital identity from another European country') do
  find('label', text: 'Sign in with a digital identity from another European country').click
  click_button('Continue')
end

And('they select Spain') do
  click_button('Select DNIe')
end

Then('they should arrive at the Spain Hub') do
  assert_text('Identificación con DNIe')
end

And('they select Estonia') do
  click_button('Select ID-kaart')
end

Then('they should arrive at the Estonia Hub') do
  assert_text('Turvaliseks autentimiseks Euroopa e-teenustes')
end 

And('they select Italy') do
  click_button('Select SPID')
end

Then('they should arrive at the Italy Hub') do
  assert_text('Italian eIDAS Login')
end 

And('they select Luxembourg') do
  click_button('Select eAccess')
end

Then('they should arrive at the Luxembourg Hub') do
  assert_text('eIDAS Authentication Service')
end 

And('they navigate through Eidas') do
  assert_text('YOUR BASIC INFORMATION')
  click_button('Next')
  assert_text('YOUR ADDITIONAL INFORMATION')
  click_button('Next')
end

And('they choose the UK as the country to verify with') do
  assert_text('Kies hoe u wilt inloggen')
  click_link('English')
  assert_text('Choose how to log in')
  select "EU Login", :from => "authnServiceId"
  click_button('Continue')
  assert_text('Which country is your ID from?')
  find('#country-GB').click
  click_button('Continue')
end

Then('they should arrive at the Verify Hub blue page') do
  assert_text('Sign in with GOV.UK Verify')
end

Then('they should arrive at the success page') do
  assert_text('Response successfully received')
  assert_text('Jack Cornelius')
  assert_text('Bauer')
  assert_text('1984-02-29')
end

Then("the user should be presented with a Hub error page indicating IDP could not sign you in") do
  assert_text('Stub Idp Demo One couldn’t sign you in')
  assert_text('You may have selected the wrong company. Check your emails and text messages for confirmation of who verified you.')
end

Then("the user should be presented with an error page") do
  assert_text('Sorry, something went wrong')
  assert_text('This may be because your session timed out or there was a system error.')
end
