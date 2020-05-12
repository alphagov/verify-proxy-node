Feature: eidas-proxy-node-smoke-test-nl-prod feature

    Scenario: Proxy node happy path - LOA Substantial
        Given   the user visits the Netherlands Connector Node Stub Page
        And     they choose the UK as the country to verify with
        Then    they should arrive at the Verify Hub blue page