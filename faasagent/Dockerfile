FROM openjdk:8-jre-alpine

WORKDIR /data
ADD target/faasagent.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
