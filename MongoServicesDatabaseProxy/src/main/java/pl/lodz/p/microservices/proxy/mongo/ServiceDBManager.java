package pl.lodz.p.microservices.proxy.mongo;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by pbubel on 01.01.17.
 */
class ServiceDBManager {
    private static final String COLLECTION_SERVICES = "Services";
    private static final Logger log = LoggerFactory.getLogger(MongoServicesDatabaseProxyService.class);

    static void getServicesList(Message<JsonObject> inMessage, MongoClient mongoClient) {
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0).put("price", 0).put("description", 0));

        mongoClient.findWithOptions(COLLECTION_SERVICES, new JsonObject(), options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load services from database succeeded.");
            } else {
                log.error("Load services from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void getServiceDetails(Message<JsonObject> inMessage, MongoClient mongoClient) {
        String name = inMessage.body().getString("name");
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0));
        JsonObject jsonQuery = new JsonObject().put("name", name);

        mongoClient.findWithOptions(COLLECTION_SERVICES, jsonQuery, options, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result);
                    log.info("Load service details from database succeeded.");
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

    static void editService(Message<JsonObject> inMessage, MongoClient mongoClient) {
        String name = inMessage.body().getString("name");
        JsonObject jsonQuery = new JsonObject().put("name", name);
        JsonObject serviceData = inMessage.body().getJsonObject("service");
        JsonObject update = new JsonObject().put("$set", serviceData);

        mongoClient.update(COLLECTION_SERVICES, jsonQuery, update, response -> {
            if (response.succeeded()) {
                log.info("Edit service data in database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(200, "Edited"));
            } else {
                log.info("Edit service data in database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void saveNewService(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject serviceJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_SERVICES, serviceJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new service to database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new service to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void deleteService(Message<JsonObject> inMessage, MongoClient mongoClient) {
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
}