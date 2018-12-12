#
#   Docker Image for Translator Component in Test environments only.
#   Translator depends on softHSM and OpenSC installations in Test environments.
#
FROM gradle:jdk11 AS build

WORKDIR /app
USER root
ENV GRADLE_USER_HOME ~/.gradle

COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY proxy-node-shared/ proxy-node-shared/

ARG component=proxy-node-gateway
COPY ${component}/src ${component}/src
COPY ${component}/build.gradle ${component}/build.gradle

RUN gradle --no-daemon --quiet -p ${component} --parallel install -x test
ENTRYPOINT ["gradle", "--no-daemon"]

# -------------------- Runtime Image --------------------
FROM govukverify/soft-hsm

# Copy java app from build to runtime image
WORKDIR /app
USER root

ARG component
COPY --from=build /app/${component}/build/install/${component} /app

# Copy test signing keys
RUN mkdir /app/keys
COPY --from=build /app/${component}/src/dist/proxy_node_signing.* /app/keys/

# softHSM Environment Variable assignment from ARGS
ARG softHSMSigningKeyLabel
ARG softHSMSigningKeyToken
ARG softHSMSigningKeySlot
ARG softHSMSigningKeyId
ARG softHSMSigningKeyPin
ARG softHSMSigningKeySoPin

ENV SOFT_HSM_SIGNING_KEY_LABEL=${softHSMSigningKeyLabel}
ENV SOFT_HSM_SIGNING_KEY_TOKEN=${softHSMSigningKeyToken}
ENV SOFT_HSM_SIGNING_KEY_SLOT=${softHSMSigningKeySlot}
ENV SOFT_HSM_SIGNING_KEY_ID=${softHSMSigningKeyId}
ENV SOFT_HSM_SIGNING_KEY_PIN=${softHSMSigningKeyPin}
ENV SOFT_HSM_SIGNING_KEY_SO_PIN=${softHSMSigningKeySoPin}

# Init softHSM slot
RUN softhsm2-util --init-token --slot ${softHSMSigningKeySlot} --so-pin ${softHSMSigningKeySoPin} --pin ${softHSMSigningKeyPin} --label ${softHSMSigningKeyLabel}

# Load signing key to softHSM
RUN softhsm2-util --import /app/keys/proxy_node_signing.p8 --token ${softHSMSigningKeyToken} --label ${softHSMSigningKeyLabel} --id ${softHSMSigningKeyId} --pin ${softHSMSigningKeyPin}
RUN rm -rf /app/keys/*

ENV CONFIG_FILE config.yml
ENV COMPONENT $component
CMD "bin/$COMPONENT"

