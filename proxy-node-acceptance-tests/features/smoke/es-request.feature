Feature: eidas-proxy-node-smoke-test-es-prod feature

    Scenario: Connector node happy path for Spain
        Given   the user visits a government service
        And     they choose sign in with a digital identity from another European country
        And     they select Spain
        Then    they should arrive at the Spain Hub
