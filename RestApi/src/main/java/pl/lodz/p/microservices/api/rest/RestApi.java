package pl.lodz.p.microservices.api.rest;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import pl.lodz.p.microservices.api.rest.method.ServicesManagementMethods;
import pl.lodz.p.microservices.api.rest.method.UsersManagementMethods;

import java.util.HashSet;
import java.util.Set;

public class RestApi extends AbstractVerticle {
    private static final String SERVICES_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.ServicesManagement";
    private static final String USERS_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.UsersManagement";

    private static final String METHOD_KEY = "method";

    private static final String SERVICES_ENDPOINT = "/api/services";
    private static final String USERS_ENDPOINT = "/api/users";

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
        router.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));

        // Services endpoint
        router.get(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICES_LIST,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS));

        router.get(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICE_DETAILS,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name"));

        router.delete(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.DELETE_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name"));

        router.post(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.SAVE_NEW_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                context.getBodyAsJson()));

        router.put(SERVICES_ENDPOINT + "/:name").handler(context -> requestHandler(context,
                ServicesManagementMethods.EDIT_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "name",
                context.getBodyAsJson()));

        // Users endpoint
        router.get(USERS_ENDPOINT).handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USERS_LIST,
                USERS_MANAGEMENT_SERVICE_ADDRESS));

        router.get(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.GET_USER_DETAILS,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login"));

        router.delete(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.DELETE_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login"));

        router.post(USERS_ENDPOINT).handler(context -> requestHandler(context,
                UsersManagementMethods.SAVE_NEW_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                context.getBodyAsJson()));

        router.put(USERS_ENDPOINT + "/:login").handler(context -> requestHandler(context,
                UsersManagementMethods.EDIT_USER,
                USERS_MANAGEMENT_SERVICE_ADDRESS,
                "login",
                context.getBodyAsJson()));

        vertx.createHttpServer().requestHandler(router::accept).listen(8094);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue));
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter, JsonObject body) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue).mergeIn(body));
    }

    private void requestHandler(RoutingContext context, Enum method, String address) {
        requestHandler(context, method, address, new JsonObject());
    }

    private void requestHandler(RoutingContext context, Enum method, String address, JsonObject body) {
        log.info("Passing request to start method: " + method.name() + " to microservice: " + address);

        final DeliveryOptions options = new DeliveryOptions()
                .setSendTimeout(3500)
                .addHeader(METHOD_KEY, method.name());

        passMessageToAnotherService(context, options, address, body);
    }

    private void passMessageToAnotherService(RoutingContext routingContext, DeliveryOptions options, String address, JsonObject jsonMessage) {
        eventBus.send(address, jsonMessage, options,
                response -> {
                    if (!response.succeeded()) {
                        ReplyException cause = (ReplyException) response.cause();
                        respond(routingContext, cause.failureCode(), cause.getMessage());
                        return;
                    }
                    JsonObject jsonObject;
                    if (response.result().body().getClass().equals(JsonObject.class)) {
                        jsonObject = (JsonObject) response.result().body();
                    } else {
                        respond(routingContext, 500, "Internal server error. Server does not respond with application/json");
                        return;
                    }
                    routingContext.response()
                            .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                            .end(jsonObject.encodePrettily());
                });
    }

    private void respond(RoutingContext routingContext, int code, String message) {
        routingContext.response()
                .putHeader("Content-Type", MediaType.JSON_UTF_8.toString())
                .setStatusCode(code)
                .end(new JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .encodePrettily());
    }
}
