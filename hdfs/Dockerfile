FROM openjdk:8-jre-alpine

WORKDIR /data

ADD target/hdfs-agent.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
