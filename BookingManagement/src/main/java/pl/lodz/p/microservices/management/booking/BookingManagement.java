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
        if (inMessage.body() == null) {
            log.error("Empty message received.");
            inMessage.fail(400, "Received method call without body");
            return;
        }
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method not found");
            inMessage.fail(500, "BookingsManagement: Method" + requestedMethod + " not found");
            return;
        }

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
        }
    }

    // FIXME niepotrzebne?
    private void getBookingsList(Message<JsonObject> inMessage) {
        log.info("Called method GET_BOOKINGS_LIST");
        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_BOOKINGS_LIST"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject replyList = new JsonObject().put("list", objectToArray(response.result().body()));
                        inMessage.reply(replyList);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // FIXME niepotrzebne?
    private void getBookingDetails(Message<JsonObject> inMessage) {
        log.info("Called method GET_BOOKING_DETAILS with message body: " + inMessage.body());

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
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_BOOKING_DETAILS"),
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

    // FIXME niepotrzebne?
    private void saveNewBooking(Message<JsonObject> inMessage) {
        log.info("Called method SAVE_NEW_BOOKING with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("booking")) {
            inMessage.fail(400, "Bad Request. Key 'booking' is required.");
            return;
        }
        JsonObject newBooking = inMessage.body().getJsonObject("booking");

        if (!newBooking.containsKey("userLogin") ||
                StringUtils.isBlank(newBooking.getString("userLogin"))) {
            inMessage.fail(400, "Bad Request. Field 'userLogin' is required.");
            return;
        }
        if (!newBooking.containsKey("serviceName") ||
                StringUtils.isBlank(newBooking.getString("serviceName"))) {
            inMessage.fail(400, "Bad Request. Field 'userLogin' is required.");
            return;
        }

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, newBooking,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_BOOKING"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // FIXME niepotrzebne?
    private void editBooking(Message<JsonObject> inMessage) {
        log.info("Called method EDIT_BOOKING with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("booking")) {
            inMessage.fail(400, "Bad Request. Booking data is required.");
            return;
        }
        if (!inMessage.body().containsKey("id")) {
            inMessage.fail(400, "Bad Request. Booking login is required.");
            return;
        }
        JsonObject query = new JsonObject().put("booking", inMessage.body().getJsonObject("booking"))
                .put("_id", inMessage.body().getString("id"));

        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_BOOKING"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // FIXME niepotrzebne?
    private void deleteBooking(Message<JsonObject> inMessage) {
        log.info("Called method DELETE_BOOKING with message body: " + inMessage.body());
        if (!inMessage.body().containsKey("id") ||
                StringUtils.isBlank(inMessage.body().getString("id"))) {
            inMessage.fail(400, "Bad Request. Field 'id' is required.");
            return;
        }
        JsonObject query = new JsonObject().put("_id", inMessage.body().getString("id"));
        eventBus.send(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, query,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_BOOKING"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // TODO move it to separate Helpers class
    private JsonArray objectToArray(JsonObject object) {
        JsonArray array = new JsonArray();
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            array.add(given.getJsonObject(i).getJsonObject("_id").getString("$oid"));
        }
        return array;
    }
}