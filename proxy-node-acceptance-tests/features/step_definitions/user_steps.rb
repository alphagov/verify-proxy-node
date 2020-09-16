require 'uri'
require 'securerandom'

def stub_idp_user
  env('STUB_IDP_USER') || "stub-idp-demo-one"
end

Given("the Proxy Node is sent an LOA {string} request from the Stub Connector") do |loa|
  loa_path = case loa
    when "Low"
      "/RequestLow"
    when "Substantial"
      "/RequestSubstantial"
    when "High"
      "/RequestHigh"
    else
      "/BadRequest"
             end
  visit(env('STUB_CONNECTOR_URL') + loa_path)
end

Given("the proxy node is sent a transient PID request") do
  visit(env('STUB_CONNECTOR_URL') + "/RequestTransientPid")
end

And('they progress through Verify') do
  assert_text('Sign in with GOV.UK Verify')
  choose('start_form_selection_false', allow_label_click: true)
  click_button('Continue')
  find('button', :text => 'Stub Idp Demo One').click
end

Given(/^the Stub Connector supplies an authentication request with (.*)$/) do |issue|
  scenario_path_map = {
      "a missing signature": "/MissingSignature",
      "an invalid signature": "/InvalidSignature"
  }
  visit(env('STUB_CONNECTOR_URL') + scenario_path_map[issue.to_sym])
end

Given('they login to Stub IDP') do
  fill_in('username', with: stub_idp_user)
  fill_in('password', with: 'bar')
  click_on('SignIn')
  click_on('I Agree')
end

Given('they login to Stub IDP with error event {string}') do |error_button_text|
  fill_in('username', with: stub_idp_user)
  fill_in('password', with: 'bar')
  click_on(error_button_text)
end

Given("the user accesses a invalid page") do
  visit(env('PROXY_NODE_URL') + '/asdfasdfasfsaf')
end

Given("the user accesses the Gateway response URL directly") do
  visit(env('PROXY_NODE_URL') + '/SAML2/SSO/Response/POST')
end

Given("the user visits the {string} Stub Connector Node page") do |country|
  visit(country_stub_connector_url(country))
end

And('they navigate the {string} journey to verify with UK identity') do |country|
  send("navigate_#{country.downcase}_journey_to_uk")
end

Then('they should arrive at the {string} success page') do |country|
  send("arrive_at_#{country.downcase}_success_page")
end

Then('they should arrive at the Verify Hub start page') do
  assert_text('Sign in with GOV.UK Verify')
end

Then('they should arrive at the Stub Connector success page') do
  assert_text('Response successfully received')
  assert_text('Saml Validity: VALID')
  assert_text('Jack Cornelius')
  assert_text('Bauer')
  assert_text('1984-02-29')
end

And('they should have a transient PID') do
  assert_text('GB/EU/_tr_')
end

And('they should have a response issued by the Proxy Node') do
  assert_text('Issuer: ' + env('PROXY_NODE_URL') + '/ServiceMetadata')
end

Then("the user should be presented with a Hub error page indicating IDP could not sign them in") do
  assert_text('Stub Idp Demo One couldn’t sign you in')
  assert_text('You may have selected the wrong company. Check your emails and text messages for confirmation of who verified you.')
end

Then("the user should be presented with an error page") do
  assert_text('Sorry, something went wrong')
  assert_text('This may be because your session timed out or there was a system error.')
end
