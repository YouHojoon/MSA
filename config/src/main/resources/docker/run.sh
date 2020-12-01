#!/bin/sh
echo "Waiting for the eureka server to start on port $EUREKASERVER_PORT"
while ! `nc -z eurekaserver  $EUREKASERVER_PORT`; do sleep 3; done
echo "Eureka Server has started"
echo "Waiting for the configuration server to start on port $CONFIGSERVER_PORT"
while ! `nc -z configserver $CONFIGSERVER_PORT`; do sleep 3; done
echo "Configuration server has started"
echo "Starting Server with Configuration Service : $CONFIGSERVER_URI";
java -Dspring.cloud.config.uri=$CONFIGSERVER_URI \
	-Dspring.profiles.active=$PROFILE \
	-jar server.jar
