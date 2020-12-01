#!/bin/sh
echo "Waiting for the configuration server to start on port $CONFIGSERVER_PORT"
while ! `nc -z configserver $CONFIGSERVER_PORT`; do sleep 3; done
sleep 5;
echo "Configuration server has started"
echo "Starting Eureka Server with Configuration Service : $CONFIGSERVER_URI";
java -Dspring.cloud.config.uri=$CONFIGSERVER_URI \
	-Dspring.profiles.active=$PROFILE \
	-jar server.jar
