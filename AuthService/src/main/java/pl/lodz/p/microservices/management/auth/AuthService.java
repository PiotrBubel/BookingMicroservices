package pl.lodz.p.microservices.management.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.commons.lang3.EnumUtils;

public class AuthService extends AbstractVerticle {

    private static final String AUTH_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.auth.AuthService";
    private static final String DATABASE_USERS_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseUsersProxyService";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static EventBus eventBus;

    @Override
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer(AUTH_SERVICE_ADDRESS, this::messageHandler);
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
            inMessage.fail(500, "AuthService: Method" + requestedMethod + " not found");
            return;
        }

        switch (Methods.valueOf(requestedMethod)) {
            case LOGIN:
                login(inMessage);
                break;
        }
    }

    private void login(Message<JsonObject> inMessage) {
        log.info("Called method LOGIN with message body: " + inMessage.body());

        if (inMessage.body() == null) {
            log.error("Received LOGIN command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("login")) {
            log.error("Received LOGIN command without user login");
            inMessage.fail(400, "Bad request. Field 'login' is required.");
            return;
        } else if (!inMessage.body().containsKey("password")) {
            log.error("Received LOGIN command without user password");
            inMessage.fail(400, "Bad request. Field 'password' is required.");
            return;
        }
        String givenPassword = inMessage.body().getString("password");
        inMessage.body().remove("password");

        eventBus.send(DATABASE_USERS_PROXY_SERVICE_ADDRESS, inMessage.body(),
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USER_DETAILS"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject result = response.result().body();
                        String password = result.getString("password");
                        result.remove("password");
                        if (password.equals(givenPassword)) {
                            inMessage.reply(result);
                        } else {
                            inMessage.fail(401, "Unauthorized. Wrong credentials.");
                        }
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }
}