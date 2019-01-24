Feature: proxy-node feature

    Scenario: Proxy node happy path
        Given the user is at Stub Connector
        And they login as "stub-idp-demo"
        Then they should arrive at the success page

    Scenario: IDP Generates Authn Failure
        Given the user is at Stub Connector
        And the stub idp supplies a "Authn Failure"
        Then the user should be presented with an hub error page

    Scenario: IDP Generates Authn Failure
        Given the stub connector supplies a bad authn request
        Then the user should be presented with an authn error page

    Scenario: Show 404 if page doesnt exist
        Given the user accesses a invalid page
        Then the user should be presented with an '404' page

    Scenario: Show 405 if route is not accessible
        Given the user accesses a route they shouldnt
        Then the user should be presented with an '405' page
