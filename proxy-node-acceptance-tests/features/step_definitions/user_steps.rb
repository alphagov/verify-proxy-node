require 'yaml'
require 'uri'
require 'securerandom'

Given('the user is at Stub Connector') do
  visit(ENV.fetch('STUB_CONNECTOR_URL'))
end

Given('they login as {string}') do |username|
  fill_in('username', with: username)
  fill_in('password', with: 'bar')
  click_on('SignIn')
  click_on('I Agree')
end

Then('they should arrive at the success page') do
  assert_text('Response successfully received')
  assert_text('Jack Cornelius')
  assert_text('Bauer')
  assert_text('1984-02-29')
end
