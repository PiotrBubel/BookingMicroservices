package pl.lodz.p.microservices.proxy.mongo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoBookingDatabaseProxyService extends AbstractVerticle {

    private static final String DATABASE_BOOKING_PROXY_SERVICE_ADDRESS = "pl.lodz.p.microservices.proxy.mongo.DatabaseBookingProxyService";
    private static final String METHOD_KEY = "method";

    private static MongoClient mongoClient;
    private static JsonObject config;

    private static final Logger log = LoggerFactory.getLogger(MongoBookingDatabaseProxyService.class);

    @Override
    public void start(Future<Void> fut) {
        config = Vertx.currentContext().config();

        log.info("Attempting to start mongo cilent with following config: " + getMongoDBConfig().encodePrettily());
        mongoClient = MongoClient.createNonShared(vertx, getMongoDBConfig());

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(DATABASE_BOOKING_PROXY_SERVICE_ADDRESS, this::messageHandler);

        mongoClient.runCommand("ping", new JsonObject().put("ping", 1), mongoPingResponse -> {
            if (mongoPingResponse.succeeded()) {
                log.info("Started Database Proxy Service with Mongo client");
                fut.complete();
            } else {
                log.error("Cannot create Database Proxy Service, cause: " + mongoPingResponse.cause().getMessage());
                fut.fail(mongoPingResponse.cause().getMessage());
            }
        });
    }

    private void messageHandler(Message<JsonObject> inMessage) {
        String calledMethod = inMessage.headers().get(METHOD_KEY);

        if (StringUtils.isBlank(calledMethod)) {
            log.warn("Incoming message have no header with method");
            inMessage.fail(400, "Message without method header");
            return;
        }

        if (!EnumUtils.isValidEnum(Methods.class, calledMethod)) {
            log.warn("Method +" + calledMethod + " not found");
            inMessage.fail(405, "Method not allowed");
            return;
        }

        Methods method = Methods.valueOf(calledMethod);

        switch (method) {
            // Bookings
            case SAVE_NEW_BOOKING:
                BookingDBManager.saveNewBooking(inMessage, mongoClient);
                break;
            case GET_BOOKINGS_LIST:
                BookingDBManager.getBookingsList(inMessage, mongoClient);
                break;
            case GET_BOOKING_DETAILS:
                BookingDBManager.getBookingDetails(inMessage, mongoClient);
                break;
            case EDIT_BOOKING:
                BookingDBManager.editBooking(inMessage, mongoClient);
                break;
            case DELETE_BOOKING:
                BookingDBManager.deleteBooking(inMessage, mongoClient);
                break;
        }
    }

    /**
     * Method loads MongoDB configuration from curent verticle configuration
     *
     * @return JsonObiect with MongoDB configuration
     */
    private JsonObject getMongoDBConfig() {
        Integer port = config.getInteger("mongo_port");
        if (port == null) {
            port = 27017;
        }

        String host = config.getString("mongo_host");
        if (StringUtils.isBlank(host)) {
            host = "mongodatabase";
        }

        String db = config.getString("mongo_db");
        if (StringUtils.isBlank(db)) {
            db = "BookingsServiceDB";
        }

        String username = config.getString("mongo_username");
        if (StringUtils.isBlank(username)) {
            username = "BookingsServiceApp";
        }

        String password = config.getString("mongo_password");
        if (StringUtils.isBlank(password)) {
            password = "BookingsServicep@Ssw0rd";
        }

        return new JsonObject()
                .put("db_name", db)
                .put("port", port)
                .put("host", host)
                .put("username", username)
                .put("password", password)
                .put("authMechanism", "SCRAM-SHA-1");
    }
}