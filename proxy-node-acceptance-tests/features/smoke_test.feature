Feature: smoke-test feature

    Scenario: Proxy node happy path - LOA Substantial
        Given the proxy node is sent a LOA 'Substantial' request
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page