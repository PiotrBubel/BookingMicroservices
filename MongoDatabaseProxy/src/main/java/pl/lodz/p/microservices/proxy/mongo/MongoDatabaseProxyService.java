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
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

public class MongoDatabaseProxyService extends AbstractVerticle {

    private static final String DATABASE_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseProxyService";
    private static final String METHOD_KEY = "method";

    private static final String COLLECTION_SERVICES = "Services";

    private static MongoClient mongoClient;
    private static JsonObject config;

    private static final Logger log = LoggerFactory.getLogger(MongoDatabaseProxyService.class);

    @Override
    public void start(Future<Void> fut) {
        config = Vertx.currentContext().config();

        log.info("Attempting to start mongo cilent with following config: " + getMongoDBConfig().encodePrettily());
        mongoClient = MongoClient.createNonShared(vertx, getMongoDBConfig());

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(DATABASE_PROXY_SERVICE_ADDRESS, this::messageHandler);

        mongoClient.runCommand("ping", new JsonObject().put("ping", 1), mongoPingResponse -> {
            if (mongoPingResponse.succeeded()) {
                log.info("Started Database Proxy Service with Mongo client");
                fut.complete();
            } else {
                log.error("Cannot create Database Proxy Service, cause: " + mongoPingResponse.cause().getMessage());
                fut.fail(mongoPingResponse.cause().getMessage());
            }
        });
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String calledMethod = inMessage.headers().get(METHOD_KEY);

        if (StringUtils.isBlank(calledMethod)) {
            log.warn("Incoming message have no header with method");
            inMessage.fail(400, "Message without method header");
            return;
        }

        if (!EnumUtils.isValidEnum(Methods.class, calledMethod)) {
            log.warn("Method +" + calledMethod + " not found");
            inMessage.fail(405, "Method not allowed");
            return;
        }

        Methods method = Methods.valueOf(calledMethod);

        switch (method) {
            case GET_SERVICES_LIST:
                getServicesList(inMessage);
                break;
            case SAVE_NEW_SERVICE:
                saveNewService(inMessage);
                break;
            case DELETE_SERVICE:
                deleteService(inMessage);
                break;
            case LOGIN:
                checkCredentials(inMessage);
                break;
            case GET_SERVICE_DETAILS:
                getServiceDetails(inMessage);
                break;
        }
    }

    private void getServicesList(Message<JsonObject> inMessage) {
        log.info("Called method GET_SERVICES_LIST");

        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0).put("time", 0).put("description", 0));

        mongoClient.findWithOptions(COLLECTION_SERVICES, new JsonObject(), options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load services from database succeeded. " + result);
            } else {
                log.error("Load services from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    private void getServiceDetails(Message<JsonObject> inMessage) {
        log.info("Called method GET_SERVICE_DETAILS");

        if (inMessage.body() == null) {
            log.error("Received GET_SERVICE_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received GET_SERVICE_DETAILS command without service name");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }
        String name = inMessage.body().getString("name");
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0));
        JsonObject jsonQuery = new JsonObject().put("name", name);

        mongoClient.findWithOptions(COLLECTION_SERVICES, jsonQuery, options, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result);
                    log.info("Load service details from database succeeded. " + result);
                } else {
                    log.info("Load service details from database not succeeded. No service with name: " + name);
                    inMessage.fail(404, "No service with name: " + name);
                }
            } else {
                log.error("Load service details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    private void saveNewService(Message<JsonObject> inMessage) {
        log.info("Called method SAVE_NEW_SERVICE with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_SERVICE command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        }

        JsonObject serviceJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_SERVICES, serviceJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new service to database succeeded. New service: " + serviceJsonObject.encodePrettily());
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new service to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    private void deleteService(Message<JsonObject> inMessage) {
        log.info("Called method DELETE_SERVICE with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received DELETE_SERVICE command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received DELETE_SERVICE command without service name");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }

        JsonObject jsonQuery = new JsonObject().put("name", inMessage.body().getValue("name"));
        mongoClient.removeOne(COLLECTION_SERVICES, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Load service details from database succeeded.");
            } else {
                log.error("Load service details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    private void checkCredentials(Message<JsonObject> inMessage) {
        log.info("Called method LOGIN with message body: " + inMessage.body());
        log.warn("NOT SUPPORTED YET");
        inMessage.fail(501, "Internal error: NOT SUPPORTED YET");
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
            username = "BookingsServiceApp";
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