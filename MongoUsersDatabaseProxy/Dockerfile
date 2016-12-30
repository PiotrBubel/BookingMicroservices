FROM vertx/vertx3

COPY target/MongoUsersDatabaseProxy-0.0.1-SNAPSHOT-fat.jar /lib/MongoUsersDatabaseProxy.jar
COPY src/config/users-database-proxy-service-conf.json /lib/users-database-proxy-service-conf.json

CMD java -jar /lib/MongoUsersDatabaseProxy.jar -ha -conf /lib/users-database-proxy-service-conf.json
