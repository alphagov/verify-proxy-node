Feature: eidas-proxy-node-smoke-test-nl-prod feature

  Scenario: Proxy Node Netherlands happy path - LOA Substantial
    Given   the user visits the "Netherlands" Stub Connector Node page
    And     they navigate the "Netherlands" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page