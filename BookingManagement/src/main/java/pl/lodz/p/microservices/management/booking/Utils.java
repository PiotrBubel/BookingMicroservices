package pl.lodz.p.microservices.management.booking;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utils class for Bookings Management Service
 */
public class Utils {

    /**
     * Method adds current date to given Json
     *
     * @return given JsonObject with added field with current date
     */
    static JsonObject addCreateDate(JsonObject json) {

        return json.put("createdDate", Utils.shortCurrentDate());
    }

    /**
     * @return formatted current date without hour, as String
     */
    static String shortCurrentDate() {
        Date d = new Date();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        return sdfDate.format(d);
    }

    static JsonObject idObjectsToFields(JsonObject object) {
        JsonObject result = new JsonObject().put("list", new JsonArray());
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            JsonObject changed = given.getJsonObject(i).copy();
            changed.remove("_id");
            changed.put("id", given.getJsonObject(i).getJsonObject("_id").getString("$oid"));
            result.getJsonArray("list").add(changed);
        }
        return result;
    }

    static JsonArray dateObjectsToArray(JsonObject object) {
        JsonArray array = new JsonArray();
        JsonArray given = object.getJsonArray("list");
        for (int i = 0; i < given.size(); i++) {
            array.add(given.getJsonObject(i).getString("date"));
        }
        return array;
    }
}
