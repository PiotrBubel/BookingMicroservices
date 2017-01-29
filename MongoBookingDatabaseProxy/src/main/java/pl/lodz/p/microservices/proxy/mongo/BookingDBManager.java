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
    private static final Logger log = LoggerFactory.getLogger(MongoBookingsDatabaseProxyService.class);

    static void getBookingsList(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject query = new JsonObject();
        if (inMessage.body().containsKey("login") && !inMessage.body().getString("login").equals("")) {
            query.put("userLogin", inMessage.body().getString("login"));
        }

        FindOptions options = new FindOptions().setFields(new JsonObject().put("description", 0).put("createdDate", 0));

        mongoClient.findWithOptions(COLLECTION_BOOKINGS, query, options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load bookings from database succeeded.");
            } else {
                log.error("Load bookings from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void getTakenDates(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject query = new JsonObject().put("serviceName", inMessage.body().getString("serviceName")
        ).put("date", new JsonObject().put("$regex", "^" + inMessage.body().getString("date")));

        FindOptions options = new FindOptions().setFields(new JsonObject().put("serviceName", 0).put("userLogin", 0)
                .put("_id", 0).put("description", 0).put("createdDate", 0));

        mongoClient.findWithOptions(COLLECTION_BOOKINGS, query, options, response -> {
            if (response.succeeded()) {
                JsonObject result = new JsonObject().put("list", response.result());
                inMessage.reply(result);
                log.info("Load taken dates from database succeeded.");
            } else {
                log.error("Load taken dates from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void getBookingDetails(Message<JsonObject> inMessage, MongoClient mongoClient) {
        String _id = inMessage.body().getString("_id");
//        JsonObject jsonQuery = new JsonObject().put("_id", new JsonObject().put("$oid", _id));
        JsonObject jsonQuery = new JsonObject().put("_id", _id);
        FindOptions options = new FindOptions().setFields(new JsonObject().put("_id", 0));

        mongoClient.findWithOptions(COLLECTION_BOOKINGS, jsonQuery, options, response -> {
            if (response.succeeded()) {
                if (response.result().size() > 0) {
                    JsonObject result = response.result().get(0);
                    inMessage.reply(result.put("id", _id));
                    log.info("Load booking details from database succeeded.");
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
        String _id = inMessage.body().getString("_id");
//        JsonObject jsonQuery = new JsonObject().put("_id", new JsonObject().put("$oid", _id));
        JsonObject jsonQuery = new JsonObject().put("_id", _id);
        JsonObject bookingData = inMessage.body().getJsonObject("booking");
        JsonObject update = new JsonObject().put("$set", bookingData);

        mongoClient.update(COLLECTION_BOOKINGS, jsonQuery, update, response -> {
            if (response.succeeded()) {
                log.info("Edit booking data in database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(200, "Edited"));
            } else {
                log.info("Edit booking data to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void saveNewBooking(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject bookingJsonObject = inMessage.body();
        mongoClient.insert(COLLECTION_BOOKINGS, bookingJsonObject, response -> {
            if (response.succeeded()) {
                log.info("Save new booking to database succeeded.");
                inMessage.reply(Utils.jsonHttpResponse(201, "Created"));
            } else {
                log.error("Save new booking to database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void deleteBooking(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject jsonQuery = new JsonObject().put("_id", inMessage.body().getString("_id"));
        mongoClient.removeOne(COLLECTION_BOOKINGS, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Remove booking from database succeeded.");
            } else {
                log.error("Remove booking from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }

    static void deleteUserBookings(Message<JsonObject> inMessage, MongoClient mongoClient) {
        JsonObject jsonQuery = new JsonObject().put("userLogin", inMessage.body().getValue("login"));
        mongoClient.removeDocuments(COLLECTION_BOOKINGS, jsonQuery, response -> {
            if (response.succeeded()) {
                inMessage.reply(Utils.jsonHttpResponse(204, "No content"));
                log.info("Remove user bookings from database succeeded.");
            } else {
                log.error("Remove user bookings from database failed, cause: " + response.cause().getMessage());
                inMessage.fail(500, "Database error: " + response.cause().getMessage());
            }
        });
    }
}