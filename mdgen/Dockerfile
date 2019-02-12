# Base builder image

from gradle:jdk11 AS build
workdir /mdgen
user root
env GRADLE_user_HOME ~/.gradle
copy build.gradle build.gradle
copy src/ src/
run gradle --no-daemon --quiet --parallel install

from openjdk:11-jre-slim
copy --from=build /mdgen/build/install/mdgen /
entrypoint ["/bin/mdgen"]
