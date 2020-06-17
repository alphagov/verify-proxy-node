Feature: eidas-proxy-node-smoke-test-integration feature

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