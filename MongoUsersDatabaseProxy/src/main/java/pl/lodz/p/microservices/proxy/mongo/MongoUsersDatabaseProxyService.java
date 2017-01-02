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

public class MongoUsersDatabaseProxyService extends AbstractVerticle {

    private static final String DATABASE_USERS_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseUsersProxyService";
    private static final String METHOD_KEY = "method";

    private static final String COLLECTION_USERS = "Users";

    private static MongoClient mongoClient;
    private static JsonObject config;

    private static final Logger log = LoggerFactory.getLogger(MongoUsersDatabaseProxyService.class);

    @Override
    public void start(Future<Void> fut) {
        config = Vertx.currentContext().config();

        log.info("Attempting to start mongo cilent with following config: " + getMongoDBConfig().encodePrettily());
        mongoClient = MongoClient.createNonShared(vertx, getMongoDBConfig());

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(DATABASE_USERS_PROXY_SERVICE_ADDRESS, this::messageHandler);

        mongoClient.runCommand("ping", new JsonObject().put("ping", 1), mongoPingResponse -> {
            if (mongoPingResponse.succeeded()) {
                log.info("Started Mongo Users Database Proxy Service with Mongo client");
                fut.complete();
            } else {
                log.error("Cannot create Mongo Users Database Proxy Service, cause: " + mongoPingResponse.cause().getMessage());
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
            case GET_USERS_LIST:
                getUsersList(inMessage);
                break;
            case GET_USER_DETAILS:
                getUserDetails(inMessage);
                break;
            case SAVE_NEW_USER:
                saveNewUser(inMessage);
                break;
            case DELETE_USER:
                deleteUser(inMessage);
                break;
            case EDIT_USER:
                editUser(inMessage);
                break;
        }
    }

    private void getUsersList(Message<JsonObject> inMessage) {
        log.info("Called method GET_USERS_LIST");

        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0)
                .put("time", 0).put("description", 0));

        mongoClient.findWithOptions(COLLECTION_USERS, new JsonObject(), options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load users from database succeeded. " + result);
            } else {
                log.error("Load users from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Users Database error: " + response.cause().getMessage());
            }
        });
    }

    private void getUserDetails(Message<JsonObject> inMessage) {
        log.info("Called method GET_USER_DETAILS");

        if (inMessage.body() == null) {
            log.error("Received GET_USER_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received GET_USER_DETAILS command without user login");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }
        String login = inMessage.body().getString("login");
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0));
        JsonObject jsonQuery = new JsonObject().put("login", login);

        mongoClient.findWithOptions(COLLECTION_USERS, jsonQuery, options, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result);
                    log.info("Load user details from database succeeded. " + result);
                } else {
                    log.info("Load user details from database not succeeded. No user with login: " + login);
                    inMessage.fail(404, "No user with login: " + login);
                }
            } else {
                log.error("Load user details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Users Database error: " + response.cause().getMessage());
            }
        });
    }

    private void editUser(Message<JsonObject> inMessage) {
        log.info("Called method EDIT_USER");

        if (inMessage.body() == null) {
            log.error("Received EDIT_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received EDIT_USER command without service login");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }
        String login = inMessage.body().getString("login");
        JsonObject jsonQuery = new JsonObject().put("login", login);
        JsonObject serviceData = inMessage.body().getJsonObject("user");
        JsonObject update = new JsonObject().put("$set", serviceData);

        mongoClient.update(COLLECTION_USERS, jsonQuery, update, response -> {
            if (response.succeeded()) {
                log.info("Save new user data to database succeeded. Edited user: " + login + " new data: " + serviceData.encode());
                inMessage.reply(Utils.jsonHttpResponse(200, "Edited"));
            } else {
                log.info("Save new user data to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Users Database error: " + response.cause().getMessage());
            }
        });
    }

    private void saveNewUser(Message<JsonObject> inMessage) {
        log.info("Called method SAVE_NEW_USER with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        }

        JsonObject userJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_USERS, userJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new user to database succeeded. New user: " + userJsonObject.encodePrettily());
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new user to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Users Database error: " + response.cause().getMessage());
            }
        });
    }

    private void deleteUser(Message<JsonObject> inMessage) {
        log.info("Called method DELETE_USER with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received DELETE_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received DELETE_SERVICE command without service login");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }

        JsonObject jsonQuery = new JsonObject().put("login", inMessage.body().getValue("login"));
        mongoClient.removeOne(COLLECTION_USERS, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Remove user from database succeeded.");
            } else {
                log.error("Remove user from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Users Database error: " + response.cause().getMessage());
            }
        });
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
            host = "mongousersdatabase";
        }

        String db = config.getString("mongo_db");
        if (StringUtils.isBlank(db)) {
            db = "BookingsServiceUsersDB";
        }

        String username = config.getString("mongo_username");
        if (StringUtils.isBlank(username)) {
            username = "BookingsServiceUsersApp";
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