package pl.lodz.p.microservices.api.rest;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import pl.lodz.p.microservices.api.rest.method.AuthServiceMethods;
import pl.lodz.p.microservices.api.rest.method.BookingManagementMethods;
import pl.lodz.p.microservices.api.rest.method.ServicesManagementMethods;
import pl.lodz.p.microservices.api.rest.method.UsersManagementMethods;

import java.util.HashSet;
import java.util.Set;

public class RestApi extends AbstractVerticle {
    private static final String SERVICES_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.ServicesManagement";
    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.users.UsersManagement";
    private static final String AUTH_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.auth.AuthService";
    private static final String BOOKINGS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.booking.BookingManagement";

    private static final String METHOD_KEY = "method";

    private static final String SERVICES_ENDPOINT = "/api/services";
    private static final String USERS_ENDPOINT = "/api/users";
    private static final String BOOKING_ENDPOINT = "/api/bookings";
    private static final String AUTHENTICATE_ENDPOINT = "/api/authenticate";

    private static final Logger log = LoggerFactory.getLogger(RestApi.class);

    private static EventBus eventBus;
    private static Router router;

    @Override
    public void start() {
        log.info("RestApi start method");

        eventBus = vertx.eventBus();
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // CORS configuration
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PUT);
        allowedMethods.add(HttpMethod.POST);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Auth-Token");
        router.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));

        // Services endpoint
        router.get(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICES_LIST,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                false));

        router.get(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICE_DETAILS,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name",
                false));

        router.delete(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.DELETE_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name",
                true));

        router.post(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.SAVE_NEW_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                parseBodyToJson(context.getBodyAsString()),
                true));

        router.put(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.EDIT_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name",
                parseBodyToJson(context.getBodyAsString()),
                true));

        // Users endpoint
        router.get(USERS_ENDPOINT).handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USERS_LIST,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                true));

        router.get(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USER_DETAILS,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                true));

        router.delete(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.DELETE_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                true));

        router.post(USERS_ENDPOINT).handler(context -> requestHandler(context,
                UsersManagementMethods.SAVE_NEW_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                parseBodyToJson(context.getBodyAsString()),
                true));

        router.put(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.EDIT_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                parseBodyToJson(context.getBodyAsString()),
                true));

        // Auth endpoint
        router.post(AUTHENTICATE_ENDPOINT).handler(context -> requestHandler(context,
                AuthServiceMethods.LOGIN,
                AUTH_SERVICE_ADDRESS,
                parseBodyToJson(context.getBodyAsString()),
                false));

        // Booking endpoint
        router.get(BOOKING_ENDPOINT).handler(context -> requestWithQueryHandler(context,
                BookingManagementMethods.GET_BOOKINGS_LIST,
                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                true));

        router.get(BOOKING_ENDPOINT + "/:id").handler(context -> requestHandler(context,
                BookingManagementMethods.GET_BOOKING_DETAILS,
                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
                "id",
                true));

        router.get(BOOKING_ENDPOINT + "/dates" + "/:name/:date").handler(context -> requestHandler(context,
                BookingManagementMethods.GET_TAKEN_DATES,
                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
                "name",
                "date",
                true));

        router.delete(BOOKING_ENDPOINT + "/:id").handler(context -> requestHandler(context,
                BookingManagementMethods.DELETE_BOOKING,
                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
                "id",
                true));

        router.post(BOOKING_ENDPOINT).handler(context -> requestHandler(context,
                BookingManagementMethods.SAVE_NEW_BOOKING,
                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
                parseBodyToJson(context.getBodyAsString()),
                true));

//        router.put(BOOKING_ENDPOINT + "/:id").handler(context -> requestHandler(context,
//                BookingManagementMethods.EDIT_BOOKING,
//                BOOKINGS_MANAGEMENT_SERVICE_ADDRESS,
//                "id",
//                parseBodyToJson(context.getBodyAsString()),
//                true));

        vertx.createHttpServer().requestHandler(router::accept).listen(8094);
    }

    private void requestWithQueryHandler(RoutingContext context, Enum method, String address, String queryParameter, boolean permissionCheck) {
        JsonObject body = new JsonObject();
        if (context.request().query() != null && !context.request().query().equals("") && context.request().query().contains(queryParameter)) {
            String[] params = context.request().query().split("&");
            for (int i = 0; i < params.length; i++) {
                if (params[i].contains(queryParameter)) {
                    String parameterValue = params[0].replace(queryParameter + "=", "");
                    body.put(queryParameter, parameterValue);
                    break;
                }
            }
        }
        requestHandler(context, method, address, body, permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter, boolean permissionCheck) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue), permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter1, String parameter2, boolean permissionCheck) {
        String parameter1Value = context.request().getParam(parameter1);
        String parameter2Value = context.request().getParam(parameter2);
        requestHandler(context, method, address, new JsonObject().put(parameter1, parameter1Value).put(parameter2, parameter2Value), permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter, JsonObject body, boolean permissionCheck) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue).mergeIn(body), permissionCheck);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, boolean permissionCheck) {
        requestHandler(context, method, address, new JsonObject(), permissionCheck);
    }

    private JsonObject parseBodyToJson(String body) {
        JsonObject bodyAsJson = null;
        try {
            bodyAsJson = new JsonObject(body);
        } catch (DecodeException e) {
            log.error("Error when parsing body: " + e.getMessage());
        }
        return bodyAsJson;
    }

    private void requestHandler(RoutingContext context, Enum method, String address, JsonObject body, boolean permissionCheck) {
        String logInfo = "Passing request to start method: " + method.name() + " to microservice: " + address;
        if (permissionCheck) {
            logInfo += " with permission check first.";
        }
        log.info(logInfo);
        if (body != null) {
            log.info("Incoming message: " + body.encodePrettily());
        } else {
            body = new JsonObject();
            log.info("Without message.");
        }

        final DeliveryOptions options = new DeliveryOptions()
                .setSendTimeout(3500)
                .addHeader(METHOD_KEY, method.name());

        if (permissionCheck) {
            passToServiceWithAuthCheck(context, options, address, body, method.name());
        } else {
            passMessageToService(context, options, address, body);
        }
    }

    private void passToServiceWithAuthCheck(RoutingContext routingContext, DeliveryOptions options, String address, JsonObject jsonMessage, String method) {
        JsonObject parameters = new JsonObject();
        if (jsonMessage.containsKey("login")) {
            parameters.put("login", jsonMessage.getString("login"));
            // parameters to sent to auth service can be added when needed
        }
        if (jsonMessage.containsKey("booking") && jsonMessage.getJsonObject("booking").containsKey("userLogin")){
            parameters.put("login", jsonMessage.getJsonObject("booking").getString("userLogin"));
        }
        JsonObject message = new JsonObject().put("token", routingContext.request().getHeader("Auth-Token"))
                .put("method", method)
                .put("parameters", parameters);

        final DeliveryOptions authOptions = new DeliveryOptions()
                .setSendTimeout(3500)
                .addHeader(METHOD_KEY, AuthServiceMethods.CHECK_PERMISSIONS.name());

        eventBus.send(AUTH_SERVICE_ADDRESS, message, authOptions, res -> {
            if (res.succeeded()) {
                passMessageToService(routingContext, options, address, jsonMessage);
            } else {
                respond(routingContext, 403, "Forbidden");
            }
        });
    }

    private void passMessageToService(RoutingContext routingContext, DeliveryOptions options, String address, JsonObject jsonMessage) {
        eventBus.send(address, jsonMessage, options,
                response -> {
                    if (!response.succeeded()) {
                        ReplyException cause = (ReplyException) response.cause();
                        respond(routingContext, cause.failureCode(), cause.getMessage());
                        return;
                    }
                    JsonObject resultBody;
                    if (response.result().body().getClass().equals(JsonObject.class)) {
                        resultBody = (JsonObject) response.result().body();
                    } else {
                        respond(routingContext, 500, "Internal server error. Server does not respond with application/json");
                        return;
                    }
                    String logInfo = "Responding with ";
                    if (resultBody.containsKey("code")) {
                        routingContext.response().setStatusCode(resultBody.getInteger("code"));
                        logInfo += resultBody.getInteger("code") + " ";
                    }
                    if (resultBody.containsKey("message")) {
                        routingContext.response().setStatusMessage(resultBody.getString("message"));
                        logInfo += resultBody.getString("message");
                    }
                    logInfo += " " + resultBody.encodePrettily();
                    log.info(logInfo);
                    routingContext.response()
                            .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                            .end(resultBody.encodePrettily());
                });
    }

    private void respond(RoutingContext routingContext, int code, String message) {
        if (code < 1) {
            code = 500;
        }
        log.info("Responding with: " + code + " " + message);
        routingContext.response()
                .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                .setStatusCode(code)
                .end(new JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .encodePrettily());
    }
}
