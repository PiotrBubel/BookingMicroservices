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

    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.UsersManagement";
    private static final String DATABASE_USERS_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseUsersProxyService";

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
        if (inMessage.body() == null) {
            log.error("Empty message received.");
            inMessage.fail(400, "Received method call without body");
            return;
        }
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method not found");
            inMessage.fail(500, "UsersManagement: Method" + requestedMethod + " not found");
            return;
        }

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

    // FIXME niepotrzebne?
    private void getUsersList(Message<JsonObject> inMessage) {
        log.info("Called method GET_USERS_LIST");
        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USERS_LIST"),
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

    // FIXME niepotrzebne?
    private void getUserDetails(Message<JsonObject> inMessage) {
        log.info("Called method GET_USER_DETAILS with message body: " + inMessage.body());

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
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USER_DETAILS"),
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
    private void saveNewUser(Message<JsonObject> inMessage) {
        log.info("Called method SAVE_NEW_USER with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("user")) {
            inMessage.fail(400, "Bad Request. Key 'user' is required.");
            return;
        }
        JsonObject newService = inMessage.body().getJsonObject("user");

        if (!newService.containsKey("login") ||
                StringUtils.isBlank(newService.getString("login"))) {
            inMessage.fail(400, "Bad Request. Fields 'login' is required.");
            return;
        }

        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, newService,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_USER"),
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
    private void editUser(Message<JsonObject> inMessage) {
        log.info("Called method EDIT_USER with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("user")) {
            inMessage.fail(400, "Bad Request. User data is required.");
            return;
        }
        if (!inMessage.body().containsKey("login")) {
            inMessage.fail(400, "Bad Request. User login is required.");
            return;
        }

        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_USER"),
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
    private void deleteUser(Message<JsonObject> inMessage) {
        log.info("Called method DELETE_USER with message body: " + inMessage.body());
        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_USER"),
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
    private JsonArray jsonObjectToArray(JsonObject object) {
        JsonArray array = new JsonArray();
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            array.add(given.getJsonObject(i).getValue("login"));
        }
        return array;
    }
}