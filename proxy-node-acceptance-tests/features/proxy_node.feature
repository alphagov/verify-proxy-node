Feature: proxy-node feature

    Scenario: Proxy node happy path
        Given the user is at Stub Connector
        And they login as "stub-idp-demo"
        Then they should arrive at the success page