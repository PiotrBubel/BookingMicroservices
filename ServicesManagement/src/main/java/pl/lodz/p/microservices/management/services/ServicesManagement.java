package pl.lodz.p.microservices.management.services;

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

public class ServicesManagement extends AbstractVerticle {

    private static final String SERVICES_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.ServicesManagement";
    private static final String DATABASE_SERVICES_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseServicesProxyService";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(ServicesManagement.class);

    private static EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(SERVICES_MANAGEMENT_SERVICE_ADDRESS, this::messageHandler);
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
            case DELETE_SERVICE:
                deleteService(inMessage);
                break;
            case SAVE_NEW_SERVICE:
                saveNewService(inMessage);
                break;
            case GET_SERVICES_LIST:
                getServicesList(inMessage);
                break;
            case GET_SERVICE_DETAILS:
                getServiceDetails(inMessage);
                break;
            case EDIT_SERVICE:
                editService(inMessage);
                break;
        }
    }

    private void getServicesList(Message<JsonObject> inMessage) {
        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_SERVICES_LIST_FROM_DATABASE"),
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

    private void getServiceDetails(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received GET_SERVICE_DETAILS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received GET_SERVICE_DETAILS command without service name");
            inMessage.fail(400, "Bad request. Field 'name' is required.");
            return;
        }

        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_SERVICE_DETAILS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void saveNewService(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received SAVE_NEW_SERVICE command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("service")) {
            inMessage.fail(400, "Bad Request. Field 'service' is required.");
            return;
        }
        JsonObject newService = inMessage.body().getJsonObject("service");
        if (!newService.containsKey("name") || !newService.containsKey("price") ||
                StringUtils.isBlank(newService.getString("name"))) {
            log.error("Received SAVE_NEW_SERVICE command without service name or price");
            inMessage.fail(400, "Bad Request. Fields 'name' and 'price' are required.");
            return;
        }

        Utils.addCreatedDate(newService);
        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, newService,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_SERVICE_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void editService(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received EDIT_SERVICE command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("service")) {
            log.error("Received EDIT_SERVICE command without service data");
            inMessage.fail(400, "Bad Request. Service data is required.");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received EDIT_SERVICE command without service name");
            inMessage.fail(400, "Bad Request. Service name is required.");
            return;
        }

        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_SERVICE_IN_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        inMessage.reply(response.result().body());
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private void deleteService(Message<JsonObject> inMessage) {
        if (inMessage.body() == null) {
            log.error("Received DELETE_SERVICE command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("name")) {
            log.error("Received DELETE_SERVICE command without service name");
            inMessage.fail(400, "Bad Request. Service name is required.");
            return;
        }

        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_SERVICE_FROM_DATABASE"),
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
            array.add(given.getJsonObject(i).getValue("name"));
        }
        return array;
    }
}