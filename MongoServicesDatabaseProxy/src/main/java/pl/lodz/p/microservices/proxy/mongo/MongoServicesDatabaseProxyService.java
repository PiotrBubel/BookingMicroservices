package pl.lodz.p.microservices.proxy.mongo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoServicesDatabaseProxyService extends AbstractVerticle {

    private static final String DATABASE_SERVICES_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseServicesProxyService";
    private static final String METHOD_KEY = "method";

    private static MongoClient mongoClient;
    private static JsonObject config;

    private static final Logger log = LoggerFactory.getLogger(MongoServicesDatabaseProxyService.class);

    @Override
    public void start(Future<Void> fut) {
        config = Vertx.currentContext().config();
        mongoClient = MongoClient.createNonShared(vertx, getMongoDBConfig());
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, this::messageHandler);

        mongoClient.runCommand("ping", new JsonObject().put("ping", 1), mongoPingResponse -> {
            if (mongoPingResponse.succeeded()) {
                log.info("Started Mongo Services Database Proxy Service with Mongo client");
                fut.complete();
            } else {
                log.error("Cannot connect to database, cause: " + mongoPingResponse.cause().getMessage());
                fut.fail(mongoPingResponse.cause().getMessage());
            }
        });
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String calledMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, calledMethod)) {
            log.warn("Method +" + calledMethod + " not found");
            inMessage.fail(405, "Method not allowed");
            return;
        }

        Methods method = Methods.valueOf(calledMethod);
        log.info("Received message. Method " + method + " will be called.");
        switch (method) {
            // Services
            case GET_SERVICES_LIST_FROM_DATABASE:
                ServiceDBManager.getServicesList(inMessage, mongoClient);
                break;
            case GET_SERVICE_DETAILS_FROM_DATABASE:
                ServiceDBManager.getServiceDetails(inMessage, mongoClient);
                break;
            case SAVE_NEW_SERVICE_IN_DATABASE:
                ServiceDBManager.saveNewService(inMessage, mongoClient);
                break;
            case DELETE_SERVICE_FROM_DATABASE:
                ServiceDBManager.deleteService(inMessage, mongoClient);
                break;
            case EDIT_SERVICE_IN_DATABASE:
                ServiceDBManager.editService(inMessage, mongoClient);
                break;
        }
    }

    /**
     * Method loads MongoDB configuration from curent verticle configuration
     *
     * @return JsonObiect with MongoDB configuration
     */
    private JsonObject getMongoDBConfig() {
        Integer port = config.getInteger("mongo_port");
        if (port == null) {
            port = 27017;
        }

        String host = config.getString("mongo_host");
        if (StringUtils.isBlank(host)) {
            host = "mongodatabase";
        }

        String db = config.getString("mongo_db");
        if (StringUtils.isBlank(db)) {
            db = "BookingsServiceDB";
        }

        String username = config.getString("mongo_username");
        if (StringUtils.isBlank(username)) {
            username = "ServicesDatabaseProxyUser";
        }

        String password = config.getString("mongo_password");
        if (StringUtils.isBlank(password)) {
            password = "BookingsServicep@Ssw0rd";
        }

        return new JsonObject()
                .put("db_name", db)
                .put("port", port)
                .put("host", host)
                .put("username", username)
                .put("password", password)
                .put("authMechanism", "SCRAM-SHA-1");
    }
}