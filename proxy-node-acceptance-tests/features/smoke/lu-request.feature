Feature: eidas-proxy-node-smoke-test-lu-prod feature

    Scenario: Connector node happy path for Luxembourg
        Given   the user visits a government service
        And     they choose sign in with a digital identity from another European country
        And     they select Luxembourg
        Then    they should arrive at the Luxembourg Hub
