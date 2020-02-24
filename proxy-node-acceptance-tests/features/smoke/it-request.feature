Feature: eidas-proxy-node-smoke-test-it-prod feature

    Scenario: Connector node happy path for Italy
        Given   the user visits a government service
        And     they choose sign in with a digital identity from another European country
        And     they select Italy
        And     they navigate through Eidas
        Then    they should arrive at the Italy Hub
