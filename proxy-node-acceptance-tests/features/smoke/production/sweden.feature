Feature: eIDAS Proxy Node Smoke Test - Sweden - Production

  Scenario: Proxy Node Sweden happy path - LOA Substantial
    Given   the user visits the "SwedenProduction" Stub Connector Node page
    And     they navigate the "Sweden" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page