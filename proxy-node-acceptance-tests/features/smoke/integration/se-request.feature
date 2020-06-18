Feature: eidas-proxy-node-smoke-test-se-integration feature

  Scenario: Proxy Node Sweden happy path - LOA Substantial
    Given   the user visits the "Sweden" Stub Connector Node page
    And     they navigate the "Sweden" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page
    And     they progress through Verify
    And     they login to Stub IDP
    Then    they should arrive at the "Sweden" success page