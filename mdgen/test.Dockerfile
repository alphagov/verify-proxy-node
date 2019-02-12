from openjdk:11-jre-slim
env WORKDIR /mdgen/
workdir $WORKDIR
copy gradlew build.gradle settings.gradle $WORKDIR
copy gradle $WORKDIR/gradle
run ./gradlew build || return 0
copy . .
run apt-get update && apt-get -yq install softhsm2 libsofthsm2 wget opensc
run wget http://shibboleth.net/downloads/tools/xmlsectool/latest/xmlsectool-2.0.0-bin.zip \
  && unzip xmlsectool-2.0.0-bin.zip \
  && rm xmlsectool-2.0.0-bin.zip
run mkdir -p /var/lib/softhsm/tokens
env XMLSECTOOL /mdgen/xmlsectool-2.0.0/xmlsectool.sh
volume /mdgen/src
entrypoint ["./pre-commit.sh"]
