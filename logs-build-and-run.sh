#!/bin/bash

yes | docker-compose stop
yes | docker-compose kill
yes | docker-compose rm
touch build-logs-2.txt
mvn clean package > build-logs-2.txt
touch run-logs-2.txt
docker-compose up --no-color > run-logs-2.txt
