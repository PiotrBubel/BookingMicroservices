package pl.lodz.p.microservices.management.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import pl.lodz.p.microservices.management.auth.method.ServicesMethods;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AuthService extends AbstractVerticle {

    private static final String AUTH_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.auth.AuthService";
    private static final String DATABASE_USERS_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseUsersProxyService";

    private static final String METHOD_KEY = "method";
    private static final int TIMEOUT = 4000;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static EventBus eventBus;

    private DES encrypter;

    @Override
    public void start(Future future) {
        eventBus = vertx.eventBus();
        try {
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            encrypter = new DES(key);
        } catch (Exception e) {
            log.error(e.getMessage());
            future.fail("Error when trying to create encrypter");
        }
        eventBus.consumer(AUTH_SERVICE_ADDRESS, this::messageHandler);
        future.complete();
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String requestedMethod = inMessage.headers().get(METHOD_KEY);

        if (!EnumUtils.isValidEnum(Methods.class, requestedMethod)) {
            log.error("Method not found");
            inMessage.fail(500, "Method" + requestedMethod + " not found");
            return;
        }

        log.info("Received message. Method " + requestedMethod + " will be called.");
        switch (Methods.valueOf(requestedMethod)) {
            case LOGIN:
                login(inMessage);
                break;

            case CHECK_PERMISSIONS:
                checkPermissions(inMessage);
                break;
        }
    }

    private void checkPermissions(Message<JsonObject> inMessage) {
        String method = "";
        if (inMessage.body() == null) {
            log.error("Received CHECK_PERMISSIONS command without json object");
            inMessage.fail(400, "Received method call without JsonObject");
            return;
        } else if (!inMessage.body().containsKey("method")) {
            log.error("Received CHECK_PERMISSIONS command without method");
            inMessage.fail(400, "Bad request. Field 'method' is required.");
            return;
        } else {
            method = inMessage.body().getString("method");
            if (!methodRequiresToken(method)) {
                log.info("Requested action: " + method + " does not require token. Authorized.");
                inMessage.reply(new JsonObject().put("code", 200).put("message", "Authorized"));
                return;
            }
        }

        if (!inMessage.body().containsKey("token") || StringUtils.isBlank(inMessage.body().getString("token"))) {
            log.error("Received CHECK_PERMISSIONS command without token");
            inMessage.fail(400, "Bad request. Field 'token' is required.");
            return;
        }

        JsonObject userData = new JsonObject();
        try {
            userData = getDataFromToken(inMessage.body().getString("token"));
        } catch (Exception e) {
            log.error("Exception when decrypting token: " + e.getMessage());
            inMessage.fail(401, "Wrong token");
        }

        boolean permissionGranted = check(userData,
                method,
                inMessage.body().getJsonObject("parameters"));

        if (permissionGranted) {
            log.info("User: " + userData.getString("login") + " is authorized to perform requested action: " + method);
            inMessage.reply(new JsonObject().put("code", 200).put("message", "Authorized"));
        } else {
            log.info("User: " + userData.getString("login") + " is NOT authorized to perform requested action: " + method);
            inMessage.fail(401, "Unauthorized.");
        }
    }

    private boolean check(JsonObject userData, String method, JsonObject parameters) {

        switch (ServicesMethods.valueOf(method)) {
            // services
            case GET_SERVICES_LIST:
            case GET_SERVICE_DETAILS:
            case GET_TAKEN_DATES:
                return true;

            case DELETE_SERVICE:
            case SAVE_NEW_SERVICE:
            case EDIT_SERVICE:
                return userData.getJsonObject("permissions").getBoolean("canManageServices");

            // bookings
            case SAVE_NEW_BOOKING:
                return true;

            case DELETE_BOOKING:
            case EDIT_BOOKING:
                return userData.getJsonObject("permissions").getBoolean("canManageBookings");

            case GET_BOOKINGS_LIST:
                if (userData.getJsonObject("permissions").getBoolean("canManageBookings")) {
                    return true;
                } else if (parameters != null && parameters.containsKey("login")) {
                    return userData.getString("login").equals(parameters.getString("login"));
                } else {
                    return false;
                }
            case GET_BOOKING_DETAILS:
                return true;

            // users
            case SAVE_NEW_USER:
                return true;

            case DELETE_USER:
            case GET_USERS_LIST:
            case EDIT_USER:
                return userData.getJsonObject("permissions").getBoolean("canManageUsers");

            case GET_USER_DETAILS:
                if (userData.getJsonObject("permissions").getBoolean("canManageUsers")) {
                    return true;
                } else if (parameters != null && parameters.containsKey("login")) {
                    return userData.getString("login").equals(parameters.getString("login"));
                } else {
                    return false;
                }

            default:
                return false;
        }
    }

    private boolean methodRequiresToken(String method) {
        switch (ServicesMethods.valueOf(method)) {
            // services
            case GET_SERVICES_LIST:
            case GET_SERVICE_DETAILS:
                return false;
            // users
            case SAVE_NEW_USER:
                return false;

            default:
                return true;
        }
    }

    private void login(Message<JsonObject> inMessage) {
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
                new DeliveryOptions().setSendTimeout(TIMEOUT).addHeader(METHOD_KEY, "GET_USER_DETAILS_FROM_DATABASE"),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        JsonObject result = response.result().body();
                        String password = result.getString("password");
                        if (password.equals(givenPassword)) {
                            log.info("Correct credentials given by user: " + inMessage.body().getString("login") + " Generating and sending token.");
                            inMessage.reply(new JsonObject().put("token", createToken(result)));
                        } else {
                            log.info("Wrong credentials given user: " + inMessage.body().getString("login") + " Unauthorized.");
                            inMessage.fail(401, "Unauthorized. Wrong credentials.");
                        }
                    } else {
                        ReplyException cause = (ReplyException) response.cause();
                        inMessage.fail(cause.failureCode(), cause.getMessage());
                    }
                });
    }

    private String createToken(JsonObject userData) {
        JsonObject dataToToken = new JsonObject()
                .put("login", userData.getString("login"))
                .put("permissions", userData.getJsonObject("permissions"));

        String token = "";
        try {
            token = encrypter.encrypt(dataToToken.encode());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return token;
    }

    private JsonObject getDataFromToken(String token) throws Exception {
        String encrypted;
        encrypted = encrypter.decrypt(token);
        return new JsonObject(encrypted);
    }
}