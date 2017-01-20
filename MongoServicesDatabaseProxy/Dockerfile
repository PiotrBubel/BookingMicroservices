FROM vertx/vertx3

COPY target/MongoServicesDatabaseProxy-0.0.1-SNAPSHOT-fat.jar /lib/MongoDatabaseProxy.jar
COPY src/config/database-proxy-service-conf.json /lib/database-proxy-service-conf.json

CMD java -jar /lib/MongoDatabaseProxy.jar -ha -conf /lib/database-proxy-service-conf.json
