#!/bin/sh

set -x

env_required() {
  echo "EnvironmentVariable $1 is required."
  exit 1
}

if [ -z "$ETCD_HOST" ]; then
  env_required "ETCD_HOST"
fi

# Run the service
java -Dlca.client.config.registry=etcdregistry -Dlca.client.config.registry.etcd.hosts=${ETCD_HOST} -jar lance-agent.jar
