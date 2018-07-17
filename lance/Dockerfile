FROM openjdk:8-jre-alpine

WORKDIR /data

ADD target/lance-agent.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
