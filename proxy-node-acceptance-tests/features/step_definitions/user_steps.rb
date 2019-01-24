require 'yaml'
require 'uri'
require 'securerandom'

Given('the user is at Stub Connector') do
  visit(ENV.fetch('PROXY_NODE_URL') + ':31100/Request')
end

Given("the stub connector supplies a bad authn request") do
  visit(ENV.fetch('PROXY_NODE_URL') + ':31100/BadRequest')
end

Given('they login as {string}') do |username|
  fill_in('username', with: username)
  fill_in('password', with: 'bar')
  click_on('SignIn')
  click_on('I Agree')
end

Given("the stub idp supplies a {string}") do |error_to_throw|
  click_on(error_to_throw)
end

Given("the user accesses a invalid page") do
  visit(ENV.fetch('PROXY_NODE_URL') + ':31200/Request')
end

Given("the user accesses a route they shouldnt") do
  visit(ENV.fetch('PROXY_NODE_URL') + ':31200/SAML2/SSO/Response/POST/')
end

Then('they should arrive at the success page') do
  assert_text('Response successfully received')
  assert_text('Jack Cornelius')
  assert_text('Bauer')
  assert_text('1984-02-29')
end

Then("the user should be presented with an authn error page") do
  assert_text('Sorry, something went wrong')
  assert_text('Error handling authn request.')
end

Then("the user should be presented with an hub error page") do
  assert_text('Sorry, something went wrong')
  assert_text('Error handling hub response.')
end


Then("the user should be presented with an {string} page") do |http_error_code|
  assert_text('Sorry, something went wrong')
  assert_text('HTTP ' + http_error_code)
end
