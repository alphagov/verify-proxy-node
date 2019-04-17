Feature: proxy-node feature

    Scenario: Proxy node happy path - LOA LOW
        Given the proxy node is sent a LOA 'Low' request
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page

    Scenario: Proxy node happy path - LOA Substantial
        Given the proxy node is sent a LOA 'Substantial' request
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page

    Scenario: Proxy node happy path - LOA High
        Given the proxy node is sent a LOA 'High' request
        Then the user should be presented with an error page

    # This probably works but needs a deployment of stub connector to test if it works.
    @ignore
    Scenario: Stub connector Generates Authn Failure
        Given the stub connector supplies a bad authn request
        Then the user should be presented with an error page

    @ignore
    Scenario: Show 404 if page doesnt exist
        Given the user accesses a invalid page
        Then the user should be presented with an error page

    @ignore
    Scenario: Show 405 if route is not accessible
        Given the user accesses a route they shouldn't
        And they progress through verify
        Then the user should be presented with an error page
