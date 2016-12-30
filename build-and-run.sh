#!/bin/bash

yes | docker-compose kill
yes | docker-compose rm
mvn clean package -DskipTests
docker-compose up