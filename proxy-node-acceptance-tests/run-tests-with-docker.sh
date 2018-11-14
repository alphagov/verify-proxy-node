#!/usr/bin/env sh
set -u

echo "Before Docker compose build"
docker-compose build
echo "Docker compose build"
docker-compose run \
               -e TEST_ENV=${TEST_ENV:-"local"} \
               test-runner -f pretty -f junit -o testreport/ "$@"
exit_status=$?
docker cp $(docker ps -a -q -f name="test-runner"):/testreport .
docker-compose down
exit $exit_status
