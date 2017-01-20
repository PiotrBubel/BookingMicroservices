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
        if (inMessage.body() == null) {
            log.error("Empty message received.");
            inMessage.fail(400, "Received method call without body");
            return;
        }
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method not found");
            inMessage.fail(500, "ServicesManagement: Method" + requestedMethod + " not found");
            return;
        }

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

    // FIXME niepotrzebne?
    private void getServicesList(Message<JsonObject> inMessage) {
        log.info("Called method GET_SERVICES_LIST");
        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_SERVICES_LIST"),
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
    private void getServiceDetails(Message<JsonObject> inMessage) {
        log.info("Called method GET_SERVICE_DETAILS with message body: " + inMessage.body());

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
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_SERVICE_DETAILS"),
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
    private void saveNewService(Message<JsonObject> inMessage) {
        log.info("Called method SAVE_NEW_SERVICE with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("service")) {
            inMessage.fail(400, "Bad Request. Key 'service' is required.");
            return;
        }
        JsonObject newService = inMessage.body().getJsonObject("service");

        if (!newService.containsKey("name") || !newService.containsKey("price") ||
                StringUtils.isBlank(newService.getString("name"))) {
            inMessage.fail(400, "Bad Request. Fields 'name' and 'price' are required.");
            return;
        }

        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, newService,
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "SAVE_NEW_SERVICE"),
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
    private void editService(Message<JsonObject> inMessage) {
        log.info("Called method EDIT_SERVICE with message body: " + inMessage.body());

        if (!inMessage.body().containsKey("service")) {
            inMessage.fail(400, "Bad Request. Service data is required.");
            return;
        }
        if (!inMessage.body().containsKey("name")) {
            inMessage.fail(400, "Bad Request. Service name is required.");
            return;
        }

        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "EDIT_SERVICE"),
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
    private void deleteService(Message<JsonObject> inMessage) {
        log.info("Called method DELETE_SERVICE with message body: " + inMessage.body());
        eventBus.send(DATABASE_SERVICES_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "DELETE_SERVICE"),
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