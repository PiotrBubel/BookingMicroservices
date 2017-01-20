package pl.lodz.p.microservices.proxy.mongo;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.vertx.core.json.JsonObject;

/**
 * Utils class for DatabaseProxy Service
 */
public class Utils {

    /**
     * Method creates JsonObject with given code and message
     *
     * @return JsonObject with html response message
     */
    public static JsonObject jsonHttpResponse(int code, String message) {
        return new JsonObject()
                .put("code", code)
                .put("message", message);
    }
}
