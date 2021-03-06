# Start pages

def country_stub_connector_url(country)
  case country
  when 'Netherlands'
    'https://demo-portal.minez.nl/demoportal/etoegang/service/eb'
  when 'Estonia'
    # 'https://tara-demo.herokuapp.com/first'
    'https://tara-demo.herokuapp.com/auth?scope=eidas'
  when 'Sweden'
    'https://qa.test.swedenconnect.se/'
  when 'Spain'
    'https://eidas.redsara.es/demosp/'
  when 'SwedenProduction'
    'https://test.swedenconnect.se/'
  when 'Italy'
    'https://www.impresainungiorno.gov.it/sso/go'
  else
    raise ArgumentError.new("Invalid country name: #{country}")
  end
end

# Netherlands

def navigate_netherlands_journey_to_uk
  assert_text('EU Login')
  find('.select-dropdown').click
  find('li span', text: 'Demo portaal - PseudoID - 21').click
  page.execute_script('document.getElementById("serviceForm").submit();')
  assert_text('Which country is your ID from?')
  find('#country-GB').click
  click_button('Continue')
end

# Estonia

def navigate_estonia_journey_to_uk
  assert_text("European Union member state's eID")
  find('.selectize-control.js-select-country.single').click
  find('div.option', text: 'United Kingdom').click
  click_button('Continue')
end

def arrive_at_estonia_success_page
  assert_text('Tere, Jack Cornelius Bauer !')
  assert_text('"acr": "substantial"')
  assert_text('date_of_birth": "1984-02-29"')
end

# Sweden

def navigate_sweden_journey_to_uk
  assert_text("Test your eID")
  click_button("Foreign eID")
  click_button("countryFlag_GB")
end

def arrive_at_sweden_success_page
  assert_text("Your eID works for authentication.")
  assert_text("Jack Cornelius")
  assert_text("Bauer")
  assert_text("1984-02-29")
  assert_text("GB")
  assert_text('Your authentication was made according to eIDAS assurance level "Substantial".')
end

# Spain

def navigate_spain_journey_to_uk
  assert_text('Demo Service Provider')
  find('#submit_button').click
  assert_text('Select an identification method')
  find("#tooltip4").find(".css3").click
  assert_text("European authentication with foreign eID")
  click_button("Belgium")
  find('li', text: 'United Kingdom').click 
  find('input[value="Login"]').click
end

# Italy

def navigate_italy_journey_to_uk
  assert_text('Cittadini Europei')
  find('#lang_en').click
  assert_text('European citizens')
  find('.button-eidas').click
  assert_text('Select your country')
  find(:label, for: '13').click
  page.execute_script('document.getElementsByName("submitButton")[0].disabled = false')
  find_button('submitButton').click
  # We've seen the Italian page refuse to auto-submit its form, possibly a race condition. If the journey stalls, give it a kick
  if page.has_title? 'eIDAS Authentication Service'
    page.execute_script('document.getElementsByName("redirectForm")[0].submit()')
  end
end
