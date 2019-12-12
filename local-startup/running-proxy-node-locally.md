# Running proxy node locally

This directory contains resources to run a Proxy Node deployment locally with Docker. This is a first attempt and while a whole journey can be successfully completed, there are certain limitations:

- The Translator (and all other components) is configured to use a file credential configuration, which means it uses a set of pk8 files on the disk to sign and encrypt messages. Hence, the HSM functionality is not tested. The next step would be to investigate whether we can run a [SoftHSM](https://www.opendnssec.org/softhsm/) instance locally to simulate a real HSM for the Translator.

- The PKI and metadata files are provided statically rather being generated dynamically. This means the provided signed metadata files and all the keys and certs can't be changed easily. This is okay for now but in the future we'd want to review the existing PKI and metadata generation scripts and use them for local running.

    The metadata xml files have been manually signed with `xmlsectool` and cannot be changed without re-signing. The metadata truststores have been generated manually with `keytool` and need to be re-generated if metadata gets re-signed with a different cert chain. The certs and private keys used by the apps are symlinked from `verify-dev-pki`. 

## Building images

There are two ways to build Docker images - use locally built artifacts or compile code in Docker (slower). By default, Docker will copy locally built artifacts to the app images from `${component}/build/install`. To produce the artifacts run `./gradlew installDist [-p ${component}]` to build distribution binaries [for a specific app] before building with Docker. This takes full advantage of gradle's caching mechanisms and makes re-building images much faster. However, Dockerfile will attempt to build artifacts from scratch if it can't find locally built ones.

Building artifacts in Docker has the disadvantage that gradle cache is unavailable in the Docker image so all dependencies have to be downloaded each time for every app and everything has to be compiled from scratch. This makes the build phase quite slow. To force Docker to build the artifacts run it with the `USE_LOCAL_BUILD` flag or export it as an environment variable.

You can also set the `VERIFY_USE_PUBLIC_BINARIES` variable to use bintray if you're not on the office network and don't want to connect to the VPN.

**Build images using locally built artifacts (default behaviour)**
```shell script
$ ./gradlew installDist [-p PROJECT_DIR]
$ docker-compose build [SERVICE]

# For example
$ ./gradlew installDist -p proxy-node-translator
$ docker-compose build translator

# Rebuild all apps and update images
$ ./gradlew [--parallel] installDist && docker-compose build [--parallel]
```

**Build images and compile code from scratch**
```shell script
# Build all apps and tell Docker to build artifacts from scratch
$ USE_LOCAL_BUILD=false docker-compose build [--parallel]

# Build a single app
$ USE_LOCAL_BUILD=false docker-compose build SERVICE

# For example
$ USE_LOCAL_BUILD=false docker-compose build translator
```

***Note:*** In the above examples, Docker refers to `SERVICE`s defined in the `docker-compose.yml` file while `gradle` refers to specific project directories in the root of the Proxy Node project. 

## Running apps

The `startup-docker.sh` script spins up all the apps in the correct order. The only limitation on doing `docker-compose up` to run all apps is that the ESP will fail to start before Stub Connector metadata is available. In the future we will change the ESP to fetch the Connector encryption cert at runtime and this will go away.

Run `startup-docker.sh --build` to re-build all app and images before running.

***Note:*** You need to have the [verify-dev-pki](https://github.com/alphagov/verify-dev-pki) repo cloned at `../verify-dev-pki` (next to `verify-proxy-node`).

Once all the apps are running, individual apps can be killed and re-started using Docker commands.
```shell script
# Get all running containers
$ docker ps
$ docker kill CONTAINER_ID

# Rebuild image if necessary

$ docker-compose up SERVICE
```
To view logs for an app run `docker logs CONTAINER_ID [--follow]`.

To shut down the whole infrastructure run `docker-compose down`.

## Debugging into a running app

The `docker-compose.yml` file sets up a Java debug port for all the apps. To debug into a running app with IntelliJ you first need to add a remote configuration. Select `Run > Edit Configurations` and add a new configuration of type `Remote`. You need to specify the module classpath and debug port which you can look up in the `docker-compose.yml` file. Hit the `Debug` button at the top of the IntelliJ window and you should see the `Connected to the target VM` message if everything is configured correctly. You can then set breakpoints and step through the code.

## Completing a Proxy Node journey locally

Start the Proxy Node apps, then go to http://localhost:6610/ to initiate a journey. Click one of the green buttons to start a journey as normal and on the next page you should see a JSON response from the VSP containing the `status: "PASSED"` property. Follow the link in the `responseGeneratorLocation` to view the list of available test cases the Compliance Tool can simulate. Then follow one of the `executeUri`s to ask the Compliance Tool to generate a SAML response with specific properties.

At this point it's useful to install the [JSONView](https://chrome.google.com/webstore/detail/jsonview/chklaanhfefbnpoihckbnefhakgolnmc?hl=en) extension for Chrome (or Firefox) to parse the URLs in the JSON payload and make them clickable instead of having to copy/paste them.  

The Tool generates a SAML response with the destination specified by the VSP. The `docker-compose.yml` file starts the VSP in development mode and provides the Gateway's URL on localhost. The VSP then instructs the Compliance Tool to generate SAML messages with the destination of `http://localhost:GATEWAY_PORT/PATH`. This is the reason we can't use the staging or integration Hub instance as we'd have to add a service configuration with an assertion consumer endpoint containing `localhost`.

## Running with a local Hub or IDP

By default, the VSP runs in [`development` mode](https://github.com/alphagov/verify-service-provider#development) which connects to an instance of the IDA Compliance Tool operated by the Verify team. The Compliance Tool returns canned SAML responses which don't include any assertions.

To fully test the response processing logic, it is possible to run the Proxy Node locally with the VSP configured to generate Authentication Requests for a locally running Hub or IDP. Run `./startup-docker.sh --local-hub` to enable this mode.

This runs the VSP with an alternative config that points it to a Hub or IDP specified in the [docker.env file](docker.env). Note that only one instance of the VSP should be run at any one time - either one with the regular Compliance Tool config or the local Hub config.

### Local Hub/IDP configuration

The default configuration works with the Verify Hub run locally using scripts in [ida-hub-acceptance-tests](https://github.com/alphagov/ida-hub-acceptance-tests). Run `./hub-startup.sh --no-test-rp` to spin up an instance of the Hub to test the Proxy Node against.

However, the configuration can be adjusted to point to any Hub or IDP running locally by changing these values in the [docker.env file](docker.env):

```dotenv
# VSP running against local Hub
HUB_METADATA_URL=http://host.docker.internal:55000/local/metadata.xml
HUB_ENTITY_ID=https://local.signin.service.gov.uk
HUB_SSO_LOCATION=http://localhost:50300/SAML2/SSO
VSP_SAML_SIGNING_KEY=
VSP_SAML_ENCRYPTION_KEY=
```

`HUB_ENTITY_ID` is the entity ID published in the Hub metadata, and `HUB_METADATA_URL` is the URL at which the metadata is reachable by the VSP from within the Docker network. If the metadata server runs on `localhost` outside of the Docker network the URL needs to point to `host.docker.internal` which is a Docker DNS pointing to `localhost` from within the Docker network.

`HUB_SSO_LOCATION` is the endpoint which the VSP will include in the SAML Authentication Requests which are posted to the Hub from the user's browser. Hence, it has to contain `localhost` if the Hub runs outside Docker as it needs to be reachable from outside the Docker network.

Adjust these values based on whether you also run your metadata and Hub or IDP inside or outside the Docker network. 

Provide base64 encoded PKCS keys for SAML signing and encryption for the VSP and configure your Hub to trust the corresponding certificates.
You will need to replace the [hub-metadata.trustore file](vsp/hub-metadata.truststore) with one that contains the Hub's metadata signing cert so the VSP can trust and import it.

Finally, the Hub or IDP will need to publish valid "federation" metadata with at least its own signing and encryption certs. You can model that metadata on this [example from verify-metadata](https://github.com/alphagov/verify-metadata/blob/master/signed/local/metadata.xml) and use the scripts in that repository to generate your own. 
