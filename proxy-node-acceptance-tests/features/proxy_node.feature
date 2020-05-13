Feature: proxy-node feature

    Scenario: Proxy node happy path - LOA LOW
        Given the proxy node is sent a LOA 'Low' request from the stub connector
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page

    Scenario: Proxy node happy path - LOA Substantial
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp
        Then they should arrive at the success page

    Scenario: Proxy node happy path - LOA High
        Given the proxy node is sent a LOA 'High' request from the stub connector
        Then the user should be presented with an error page

    Scenario: Stub connector generates an Authn request with a missing signature
        Given the stub connector supplies an authn request with a missing signature
        Then the user should be presented with an error page

    Scenario: Stub connector generates an Authn request with an invalid signature
        Given the stub connector supplies an authn request with an invalid signature
        Then the user should be presented with an error page

    Scenario: Show error page if page doesnt exist
        Given the user accesses a invalid page
        Then the user should be presented with an error page

    Scenario: Show error page if route is not accessible
        Given the user accesses the gateway response url directly
        Then the user should be presented with an error page

    Scenario: Show IDP error page if No Authn Context Event
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp with error event 'No Authn Context Event'
        Then the user should be presented with a Hub error page indicating IDP could not sign you in

    Scenario: Show IDP error page if Authn Failure Event
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp with error event 'Authn Failure'
        Then the user should be presented with a Hub error page indicating IDP could not sign you in

    Scenario: Show IDP error page if Submit Requester Error Event
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp with error event 'Submit Requester Error'
        Then the user should be presented with a Hub error page indicating IDP could not sign you in

    Scenario: Show IDP error page if Submit Fraud Event
        Given the proxy node is sent a LOA 'Substantial' request from the stub connector
        And they progress through verify
        And they login to stub idp with error event 'Submit Fraud Event'
        Then the user should be presented with a Hub error page indicating IDP could not sign you in
