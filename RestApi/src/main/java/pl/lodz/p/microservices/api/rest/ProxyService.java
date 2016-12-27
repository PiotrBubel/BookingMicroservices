package pl.lodz.p.microservices.api.rest;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import pl.lodz.p.microservices.api.rest.method.ServicesManagementMethods;

public class ProxyService extends AbstractVerticle {
    private static final String SERVICES_MANAGEMENT_SERVICE_ADDRESS = "pl.lodz.p.microservices.management.services.ServicesManagement";

    private static final String METHOD_KEY = "method";

    private static final String SERVICES_ENDPOINT = "/api/services";
    private static final String USERS_ENDPOINT = "/api/users";

    private static final Logger log = LoggerFactory.getLogger(ProxyService.class);

    private static EventBus eventBus;
    private static Router router;

    @Override
    public void start() {
        log.info("ProxyService start method");

        eventBus = vertx.eventBus();
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Services endpoint
        router.get(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICES_LIST,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS));

        router.get(SERVICES_ENDPOINT + "/:serviceName").handler(context -> requestHandler(context,
                ServicesManagementMethods.GET_SERVICE_DETAILS,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "serviceName"));

        router.delete(SERVICES_ENDPOINT + "/:serviceName").handler(context -> requestHandler(context,
                ServicesManagementMethods.DELETE_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                "serviceName"));

        router.post(SERVICES_ENDPOINT).handler(context -> requestHandler(context,
                ServicesManagementMethods.SAVE_NEW_SERVICE,
                SERVICES_MANAGEMENT_SERVICE_ADDRESS,
                context.getBodyAsJson()));

        vertx.createHttpServer().requestHandler(router::accept).listen(8094);
    }

    private void requestHandler(RoutingContext context, Enum method, String address, String parameter) {
        String parameterValue = context.request().getParam(parameter);
        requestHandler(context, method, address, new JsonObject().put(parameter, parameterValue));
    }

    private void requestHandler(RoutingContext context, Enum method, String address) {
        requestHandler(context, method, address, new JsonObject());
    }

    private void requestHandler(RoutingContext context, Enum method, String address, JsonObject body) {
        log.info("Trying to start method " + method.name());

        final DeliveryOptions options = new DeliveryOptions()
                .setSendTimeout(3500)
                .addHeader(METHOD_KEY, method.name());

        passMessageToAnotherService(context, options, address, body);
    }

    private void passMessageToAnotherService(RoutingContext routingContext, DeliveryOptions options, String address, JsonObject jsonMessage) {
        log.info("Trying to get to verticle with address: " + address);

        eventBus.send(address, jsonMessage, options,
                response -> {
                    if (!response.succeeded()) {
                        log.info("Response not succeeded:" + response.cause().getMessage());
                        respond(routingContext, 500, response.cause().getMessage());
                        return;
                    }
                    log.info("Response succeeded:" + response.result().body());

                    JsonObject jsonObject;

                    if (response.result().body().getClass().equals(JsonObject.class)) {
                        jsonObject = (JsonObject) response.result().body();
                    } else {
                        respond(routingContext, 500, "Internal server error. Server does not respond with application/json");
                        return;
                    }

                    if (jsonObject.containsKey("error")) {
                        respond(routingContext, 500, "Internal error. " + jsonObject.getString("error"));
                        return;
                    }

                    routingContext.response()
                            .putHeader("content-type", MediaType.JSON_UTF_8.toString())
                            .end(jsonObject.encodePrettily());
                });
    }

    private void respond(RoutingContext routingContext, int code, String message) {
        routingContext.response()
                .putHeader("content-type", MediaType.JSON_UTF_8.toString())
                .setStatusCode(code)
                .end(new JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .encodePrettily());
    }
}