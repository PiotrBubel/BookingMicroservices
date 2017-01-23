package pl.lodz.p.microservices.management.booking;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public class BookingManagement extends AbstractVerticle {

    private static final String BOOKINGS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.booking.BookingManagement";
    private static final String DATABASE_BOOKING_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseBookingProxyService";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(BookingManagement.class);

    private static EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(BOOKINGS_MANAGEMENT_SERVICE_ADDRESS, this::messageHandler);
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method" + requestedMethod + " not found");
            inMessage.fail(500, "Method" + requestedMethod + " not found");
            return;
        }

        log.info("Received message. Method " + requestedMethod + " will be called.");
        switch (Methods.valueOf(requestedMethod)) {
            case DELETE_BOOKING:
                deleteBooking(inMessage);
                break;
            case SAVE_NEW_BOOKING:
                saveNewBooking(inMessage);
                break;
            case GET_BOOKINGS_LIST:
                getBookingsList(inMessage);
                break;
            case GET_BOOKING_DETAILS:
                getBookingDetails(inMessage);
                break;
            case EDIT_BOOKING:
                editBooking(inMessage);
                break;
            case GET_TAKEN_DATES:
                getTakenDatesList(inMessage);
                break;
            case DELETE_USER_BOOKINGS:
                deleteUserBookings(inMessage);
                break;
        }
    }

    private void getBookingsList(Message<JsonObject> inMessage) {
        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_BOOKINGS_LIST_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject replyList = Utils.idObjectsToFields(response.result().body());
                        inMessage.reply(replyList);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void getBookingDetails(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_BOOKING_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("id")) {
            log.error("Received GET_BOOKING_DETAILS command without booking id");
            inMessage.fail(400, "Bad request. Field 'id' is required.");
            return;
        }
        JsonObject query = new JsonObject().put("_id", inMessage.body().getString("id"));

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_BOOKING_DETAILS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject result = response.result().body();
                        inMessage.reply(result);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }


    private void getTakenDatesList(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_TAKEN_DATES_LIST command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name") || StringUtils.isBlank(inMessage.body().getString("name"))) {
            log.error("Received GET_TAKEN_DATES_LIST command without service name");
            inMessage.fail(400, "Bad request. Field 'serviceName' is required.");
            return;
        } else if (!inMessage.body().containsKey("date") || StringUtils.isBlank(inMessage.body().getString("date"))) {
            log.error("Received GET_TAKEN_DATES_LIST command without date prefix");
            inMessage.fail(400, "Bad request. Field 'date' is required.");
            return;
        }

        JsonObject query = new JsonObject().put("serviceName", inMessage.body().getString("name")
        ).put("date", inMessage.body().getString("date"));

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_TAKEN_DATES_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject replyList = new JsonObject().put("list", Utils.dateObjectsToArray(response.result().body()));
                        inMessage.reply(replyList);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void saveNewBooking(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("booking")) {
            log.error("Received SAVE_NEW_BOOKING command without json object");
            inMessage.fail(400, "Bad Request. Key 'booking' is required.");
            return;
        }
        JsonObject newBooking = inMessage.body().getJsonObject("booking");

        if (!newBooking.containsKey("userLogin") ||
                StringUtils.isBlank(newBooking.getString("userLogin"))) {
            log.error("Received SAVE_NEW_BOOKING command without user login");
            inMessage.fail(400, "Bad Request. Field 'userLogin' is required.");
            return;
        } else if (!newBooking.containsKey("serviceName") ||
                StringUtils.isBlank(newBooking.getString("serviceName"))) {
            log.error("Received SAVE_NEW_BOOKING command without service name");
            inMessage.fail(400, "Bad Request. Field 'serviceName' is required.");
            return;
        } if (!newBooking.containsKey("date") ||
                StringUtils.isBlank(newBooking.getString("date"))) {
            log.error("Received SAVE_NEW_BOOKING command without date");
            inMessage.fail(400, "Bad Request. Field 'date' is required.");
            return;
        }

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, Utils.addCreateDate(newBooking),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_BOOKING_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }


    // FIXME nie wiem czy bedzie wykorzystywane
    private void editBooking(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received EDIT_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("booking")) {
            log.error("Received EDIT_BOOKING command without booking data");
            inMessage.fail(400, "Bad Request. Booking data is required.");
            return;
        }
        if (!inMessage.body().containsKey("id")) {
            log.error("Received EDIT_BOOKING command without booking id");
            inMessage.fail(400, "Bad Request. Booking id is required.");
            return;
        }
        JsonObject query = new JsonObject().put("booking", inMessage.body().getJsonObject("booking"))
                .put("_id", inMessage.body().getString("id"));

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_BOOKING_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void deleteBooking(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received DELETE_BOOKING command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("id") ||
                StringUtils.isBlank(inMessage.body().getString("id"))) {
            log.error("Received DELETE_BOOKING command without booking id");
            inMessage.fail(400, "Bad Request. Field 'id' is required.");
            return;
        }
        JsonObject query = new JsonObject().put("_id", inMessage.body().getString("id"));
        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_BOOKING_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void deleteUserBookings(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received DELETE_USER_BOOKINGS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login") ||
                StringUtils.isBlank(inMessage.body().getString("login"))) {
            log.error("Received DELETE_USER_BOOKINGS command without user login");
            inMessage.fail(400, "Bad Request. Field 'login' is required.");
            return;
        }
        JsonObject query = new JsonObject().put("login", inMessage.body().getString("login"));
        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_USER_BOOKINGS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }
}