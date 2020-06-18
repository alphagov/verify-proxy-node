Feature: eidas-proxy-node-smoke-test-ee-integration feature

  Scenario: Proxy Node Estonia happy path - LOA Substantial
    Given   the user visits the "Estonia" Stub Connector Node page
    And     they navigate the "Estonia" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page
    And     they progress through Verify
    And     they login to Stub IDP
    Then    they should arrive at the "Estonia" success page