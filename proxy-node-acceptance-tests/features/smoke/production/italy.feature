Feature: eIDAS Proxy Node Smoke Test - Italy - Production

  Scenario: Proxy Node Italy happy path - LOA Substantial
    Given   the user visits the "Italy" Stub Connector Node page
    And     they navigate the "Italy" journey to verify with UK identity
    Then    they should arrive at the Verify Hub start page