Feature: Proxy Node acceptance tests

  Scenario: Proxy Node happy path - LOA LOW
    Given the Proxy Node is sent an LOA 'Low' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP
    Then  they should arrive at the Stub Connector success page
    And  they should have a response issued by the Proxy Node

  Scenario: Proxy Node happy path - LOA Substantial
    Given the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP
    Then  they should arrive at the Stub Connector success page
    And  they should have a response issued by the Proxy Node

  Scenario: Proxy Node happy path - Transient PID requested
    Given the proxy node is sent a transient PID request
    And   they progress through Verify
    And   they login to Stub IDP
    Then  they should arrive at the Stub Connector success page
    And   they should have a transient PID
    And  they should have a response issued by the Proxy Node

  Scenario: Proxy Node happy path - LOA High
    Given the Proxy Node is sent an LOA 'High' request from the Stub Connector
    Then  the user should be presented with an error page

  Scenario: Stub Connector generates an Authn request with a missing signature
    Given the Stub Connector supplies an authentication request with a missing signature
    Then  the user should be presented with an error page

  Scenario: Stub Connector generates an Authn request with an invalid signature
    Given the Stub Connector supplies an authentication request with a missing signature
    Then  the user should be presented with an error page

  Scenario: Show error page if page doesn't exist
    Given the user accesses a invalid page
    Then  the user should be presented with an error page

  Scenario: Show error page if route is not accessible
    Given the user accesses the Gateway response URL directly
    Then  the user should be presented with an error page

  Scenario: Show IDP error page if No Authn Context Event
    Given the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP with error event 'No Authn Context Event'
    Then  the user should be presented with a Hub error page indicating IDP could not sign them in

  Scenario: Show IDP error page if Authn Failure Event
    Given the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP with error event 'Authn Failure'
    Then  the user should be presented with a Hub error page indicating IDP could not sign them in

  Scenario: Show IDP error page if Submit Requester Error Event
    Given the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP with error event 'Submit Requester Error'
    Then  the user should be presented with a Hub error page indicating IDP could not sign them in

  Scenario: Show IDP error page if Submit Fraud Event
    Given the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And   they progress through Verify
    And   they login to Stub IDP with error event 'Submit Fraud Event'
    Then  the user should be presented with a Hub error page indicating IDP could not sign them in
