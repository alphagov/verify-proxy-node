# eIDAS Notification

## How to start the eidas-proxy-node application

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/eidas-proxy-node-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running POST to url `http://localhost:6600/verify-uk`

