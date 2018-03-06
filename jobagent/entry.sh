#!/bin/sh

set -x

DEFAULT_JPA_DIALECT="org.hibernate.dialect.MySQL5Dialect"
DEFAULT_JPA_DRIVER="org.mariadb.jdbc.Driver"
DEFAULT_KAFKA_GROUP_ID="job"
DEFAULT_KAFKA_RESPONSE_TIMEOUT=10000

env_required() {
  echo "EnvironmentVariable $1 is required."
  exit 1
}

env_set_default() {
  echo "EnvironmentVariable $1 is not set. Defaulting to $2."
}

buildJpaUrl() {
  export JPA_URL="jdbc:mysql://$JPA_HOST:3306/$JPA_DATABASE"
  echo "Using $JPA_URL as jdbc connection string"
}
validateMandatory() {

  if [ -z "$JPA_USER" ]; then
	  env_required "JPA_USER"
  fi

  if [ -z "$JPA_PASSWORD" ]; then
	  env_required "JPA_PASSWORD"
  fi

  if [ -z "$JPA_HOST" ]; then
	  env_required "JPA_HOST"
  fi

  if [ -z "$JPA_DATABASE" ]; then
	  env_required "JPA_HOST"
  fi

  if [ -z "$KAFKA_BOOTSTRAP_SERVERS" ]; then
	  env_required "KAFKA_BOOTSTRAP_SERVERS"
  fi

}

setDefaults() {

  if [ -z "$JPA_DIALECT" ]; then
    export JPA_DIALECT=${DEFAULT_JPA_DIALECT}
	  env_set_default "JPA_DIALECT" "$DEFAULT_JPA_DIALECT"
  fi

  if [ -z "$JPA_DRIVER" ]; then
    export JPA_DRIVER=${DEFAULT_JPA_DRIVER}
	  env_set_default "JPA_DRIVER" "$DEFAULT_JPA_DRIVER"
  fi

  if [ -z "$KAFKA_GROUP_ID" ]; then
    export KAFKA_GROUP_ID=${DEFAULT_KAFKA_GROUP_ID}
	  env_set_default "KAFKA_GROUP_ID" "$DEFAULT_KAFKA_GROUP_ID"
  fi

  if [ -z "$KAFKA_RESPONSE_TIMEOUT" ]; then
    export KAFKA_RESPONSE_TIMEOUT=${DEFAULT_KAFKA_RESPONSE_TIMEOUT}
	  env_set_default "KAFKA_RESPONSE_TIMEOUT" "$DEFAULT_KAFKA_RESPONSE_TIMEOUT"
  fi
}

validateMandatory
setDefaults
buildJpaUrl

# Run the service
java -Djpa.url=${JPA_URL} -Djpa.user=${JPA_USER} -Djpa.password=${JPA_PASSWORD} -Djpa.dialect=${JPA_DIALECT} -Djpa.driver=${JPA_DRIVER} -Dkafka.groupId=${KAFKA_GROUP_ID} -Dkafka.responseTimeout=${KAFKA_RESPONSE_TIMEOUT} -Dkafka.bootstrapServers=${KAFKA_BOOTSTRAP_SERVERS} -jar discovery-agent.jar
