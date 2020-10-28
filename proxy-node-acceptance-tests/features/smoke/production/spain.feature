Feature: eIDAS Proxy Node Smoke Test - Spain - Production

  Scenario: Proxy Node Spain happy path - LOA Substantial
    Given   the user visits the "Spain" Stub Connector Node page
    And     they navigate the "Spain" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page