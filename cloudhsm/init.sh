#!/usr/bin/env bash

set -eux

# Start the cloudhsm-client
/opt/cloudhsm/bin/configure -a $HSM_IP

# Use TCPSOCKET
sed 's/UNIXSOCKET/TCPSOCKET/g' < /opt/cloudhsm/etc/cloudhsm_client.cfg > tmp
mv tmp /opt/cloudhsm/etc/cloudhsm_client.cfg

exec $@
