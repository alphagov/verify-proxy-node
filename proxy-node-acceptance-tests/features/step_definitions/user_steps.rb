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
  find('label', :text => 'I’ve used Verify before').click
  click_button('Continue')
  find('button', :text => 'Stub Idp Demo One').click
end

Given("the stub connector supplies a bad authn request") do
  visit(ENV.fetch('STUB_CONNECTOR_URL') + '/BadRequest')
end

Given('they login to stub idp') do
  fill_in('username', with: ENV.fetch('STUB_IDP_USER'))
  fill_in('password', with: 'bar')
  click_on('SignIn')
  click_on('I Agree')
end

Given("the user accesses a invalid page") do
  visit(ENV.fetch('PROXY_NODE_URL') + '/asdfasdfasfsaf')
end

Given("the user accesses a route they shouldn't") do
  visit(ENV.fetch('PROXY_NODE_URL') + '/SAML2/SSO/Response/POST')
end

Then('they should arrive at the success page') do
  assert_text('Response successfully received')
  assert_text('Jack Cornelius')
  assert_text('Bauer')
  assert_text('1984-02-29')
end

Then("the user should be presented with an hub error page") do
  assert_text('Stub Idp Demo One couldn’t sign you in')
  assert_text('You may have selected the wrong company. Check your emails and text messages for confirmation of who verified you.')
end


Then("the user should be presented with an {string} page") do |http_error_code|
  assert_text('Sorry, something went wrong')
  assert_text('HTTP ' + http_error_code)
end
