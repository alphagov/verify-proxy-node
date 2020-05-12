Feature: eidas-proxy-node-smoke-test-integration feature

    Scenario: Integration proxy node happy path - LOA Substantial
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page