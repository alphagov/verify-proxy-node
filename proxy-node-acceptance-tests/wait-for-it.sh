printf "Waiting for Stub Connector"
until $(curl --output /dev/null --silent --head --fail "http://selenium-hub:4444/status"); do
  printf '.'
  sleep 2
done
echo
