Feature: eIDAS Proxy Node Node Smoke Test Integration

  This tests our integration instance of the Proxy Node

  Scenario: Integration Proxy Node happy path - LOA Substantial
    Given   the Proxy Node is sent an LOA 'Substantial' request from the Stub Connector
    And     they progress through Verify
    And     they login to Stub IDP
    Then    they should arrive at the Stub Connector success page

  Scenario: Integration Proxy Node happy path - LOA Low
    Given   the Proxy Node is sent an LOA 'Low' request from the Stub Connector
    And     they progress through Verify
    And     they login to Stub IDP
    Then    they should arrive at the Stub Connector success page