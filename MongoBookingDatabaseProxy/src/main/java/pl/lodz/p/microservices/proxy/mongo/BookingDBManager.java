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
class BookingDBManager {
    private static final String COLLECTION_BOOKINGS = "Bookings";
    private static final Logger log = LoggerFactory.getLogger(MongoBookingDatabaseProxyService.class);

    static void getBookingsList(Message<JsonObject> inMessage, MongoClient mongoClient) {
        log.info("Called method GET_BOOKINGS_LIST");

        FindOptions options = new FindOptions().setFields(new JsonObject().put("serviceName", 0).put("userLogin", 0)
                .put("startDate", 0).put("periods", 0).put("description", 0));

        mongoClient.findWithOptions(COLLECTION_BOOKINGS, new JsonObject(), options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load bookings from database succeeded. " + result);
            } else {
                log.error("Load bookings from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void getBookingDetails(Message<JsonObject> inMessage, MongoClient mongoClient) {
        log.info("Called method GET_BOOKING_DETAILS");

        if (inMessage.body() == null) {
            log.error("Received GET_BOOKING_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("_id")) {
            log.error("Received GET_BOOKING_DETAILS command without booking _id");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }
        String _id = inMessage.body().getString("_id");
        JsonObject jsonQuery = new JsonObject().put("_id", _id);

        mongoClient.find(COLLECTION_BOOKINGS, jsonQuery, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result);
                    log.info("Load booking details from database succeeded. " + result);
                } else {
                    log.info("Load booking details from database not succeeded. No booking with id: " + _id);
                    inMessage.fail(404, "No booking with _id: " + _id);
                }
            } else {
                log.error("Load booking details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void editBooking(Message<JsonObject> inMessage, MongoClient mongoClient) {
        log.info("Called method EDIT_BOOKING");

        if (inMessage.body() == null) {
            log.error("Received EDIT_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("_id")) {
            log.error("Received EDIT_BOOKING command without booking _id");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }
        String _id = inMessage.body().getString("_id");
        JsonObject jsonQuery = new JsonObject().put("_id", _id);
        JsonObject bookingData = inMessage.body().getJsonObject("booking");
        JsonObject update = new JsonObject().put("$set", bookingData);

        mongoClient.update(COLLECTION_BOOKINGS, jsonQuery, update, response -> {
            if (response.succeeded()) {
                log.info("Save new booking data to database succeeded. Edited booking: " + _id + " new data: " + bookingData.encode());
                inMessage.reply(Utils.jsonHttpResponse(200, "Edited"));
            } else {
                log.info("Save new booking data to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void saveNewBooking(Message<JsonObject> inMessage, MongoClient mongoClient) {
        log.info("Called method SAVE_NEW_BOOKING with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        }

        JsonObject bookingJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_BOOKINGS, bookingJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new booking to database succeeded. New booking: " + bookingJsonObject.encodePrettily());
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new booking to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void deleteBooking(Message<JsonObject> inMessage, MongoClient mongoClient) {
        log.info("Called method DELETE_BOOKING with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received DELETE_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("_id")) {
            log.error("Received DELETE_BOOKING command without booking _id");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }

        JsonObject jsonQuery = new JsonObject().put("_id", inMessage.body().getValue("_id"));
        mongoClient.removeOne(COLLECTION_BOOKINGS, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Load booking details from database succeeded.");
            } else {
                log.error("Load booking details from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }
}