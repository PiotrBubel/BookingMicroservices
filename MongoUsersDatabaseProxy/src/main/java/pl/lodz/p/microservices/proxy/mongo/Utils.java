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

    /**
     * Method adds current date to given Json
     *
     * @return given JsonObject with added field with current date
     */
    public static JsonObject addCurrentDate(JsonObject json) {

        return json.put("lotteryDate", Utils.currentDate());
    }

    /**
     * @return formatted current date as String
     */
    public static String currentDate() {
        Date d = new Date();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdfDate.format(d);
    }

    /**
     * @return formatted current date without hour, as String
     */
    public static String shortCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        return sdfDate.format(d);
    }
}
