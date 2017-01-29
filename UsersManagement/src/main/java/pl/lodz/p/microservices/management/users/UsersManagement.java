package pl.lodz.p.microservices.management.users;

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

public class UsersManagement extends AbstractVerticle {

    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.users.UsersManagement";
    private static final String DATABASE_USERS_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseUsersProxyService";
    private static final String BOOKINGS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.booking.BookingManagement";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(UsersManagement.class);

    private static EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(USERS_MANAGEMENT_SERVICE_ADDRESS, this::messageHandler);
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
            case DELETE_USER:
                deleteUser(inMessage);
                break;
            case SAVE_NEW_USER:
                saveNewUser(inMessage);
                break;
            case GET_USERS_LIST:
                getUsersList(inMessage);
                break;
            case GET_USER_DETAILS:
                getUserDetails(inMessage);
                break;
            case EDIT_USER:
                editUser(inMessage);
                break;
        }
    }

    private void getUsersList(Message<JsonObject> inMessage) {
        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USERS_LIST_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject replyList = new JsonObject().put("list", jsonObjectToArray(response.result().body()));
                        inMessage.reply(replyList);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void getUserDetails(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_USER_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received GET_USER_DETAILS command without user login");
            inMessage.fail(400, "Bad request. Field 'login' is required.");
            return;
        }

        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USER_DETAILS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject result = response.result().body();
                        result.remove("password");
                        inMessage.reply(result);
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void saveNewUser(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("user")) {
            log.error("Received SAVE_NEW_USER command without user data");
            inMessage.fail(400, "Bad Request. Key 'user' is required.");
            return;
        }
        JsonObject newUser = inMessage.body().getJsonObject("user");

        if (!newUser.containsKey("login") ||
                StringUtils.isBlank(newUser.getString("login"))) {
            log.error("Received SAVE_NEW_USER command without user login");
            inMessage.fail(400, "Bad Request. Fields 'login' is required.");
            return;
        }
        newUser.remove("permissions");
        newUser.put("permissions", new JsonObject()
                .put("canManageUsers", false)
                .put("canManageServices", false)
                .put("canManageBookings", false));
        Utils.addCreatedDate(newUser);
        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, newUser,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_USER_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void editUser(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received EDIT_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("user")) {
            log.error("Received EDIT_USER command without user data");
            inMessage.fail(400, "Bad Request. User data is required.");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received EDIT_USER command without user login");
            inMessage.fail(400, "Bad Request. User login is required.");
            return;
        }

        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_USER_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void deleteUser(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received DELETE_USER command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received DELETE_USER command without user login");
            inMessage.fail(400, "Received method call without valid JsonObject");
            return;
        }

        eventBus.send(BOOKINGS_MANAGEMENT_SERVICE_ADDRESS, inMessage.body(), new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_USER_BOOKINGS"),
                (AsyncResult<Message<JsonObject>> bookingsResponse) -> {
                    if (bookingsResponse.succeeded()) {
                        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_USER_FROM_DATABASE"),
                                (AsyncResult<Message<JsonObject>> userResponse) -> {
                                    if (userResponse.succeeded()) {
                                        inMessage.reply(userResponse.result().body());
                                    } else {
                                        ReplyException cause = (ReplyException) userResponse.cause();
                                        inMessage.fail(cause.failureCode(), cause.getMessage());
                                    }
                                });
                    } else {
                        ReplyException cause = (ReplyException) bookingsResponse.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    // TODO move it to separate Helpers class
    private JsonArray jsonObjectToArray(JsonObject object) {
        JsonArray array = new JsonArray();
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            array.add(given.getJsonObject(i).getValue("login"));
        }
        return array;
    }
}