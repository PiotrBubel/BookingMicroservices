package pl.lodz.p.microservices.management.booking;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.joda.time.DateTime;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
//import pl.lodz.p.microservices.management.users.UsersManagement;

public class DatabaseProxyServiceMock extends AbstractVerticle {

    private static final String VERTICLE_ADDRESS = "net.atos.pip.davay.bilet.DatabaseProxyService";

    private static final String METHOD = "method";

    private static final Logger log = LoggerFactory.getLogger(BookingManagement.class);

    private static EventBus eventBus;

    @Override
    public void start() {

        eventBus = vertx.eventBus();

        MessageConsumer<JsonObject> generalConsumer = eventBus.consumer(VERTICLE_ADDRESS);
        generalConsumer.handler(this::messageHandler);
    }

    private void messageHandler(Message<JsonObject> message) {
        if (message.body() == null) {
            log.error("Empty message received.");
            return;
        }

        if(message.headers().get(METHOD) == null) {
            log.error("Empty header");
        }

        String method = message.headers().get(METHOD);

        log.info("Starting " + method);

        try {
            switch (method) {
                case "LOAD_PERSONAL_DATA":
                    loadPersonalStatistics(message);
                    break;
                case "LOAD_LOTTERY_DATA":
                    loadLotteryData(message);
                    break;
                case "LOAD_LOTTERY_DATES":
                    loadLotteryDates(message);
                    break;
                default:
                    throw new NoSuchMethodException(method);
            }
        } catch (NoSuchMethodException e) {
            log.warn("There is no method: " + e.getMessage());
        }
    }

    private void loadPersonalStatistics(Message<JsonObject> message) {
        JsonObject mockStatistic = new JsonObject();
        JsonArray draws = new JsonArray();
        JsonObject draw1 = new JsonObject();
        JsonObject draw2 = new JsonObject();
        JsonArray winners = new JsonArray();
        JsonArray winners2 = new JsonArray();
        winners.add(new JsonObject().put("name", "Janusz")
                .put("surname", "Nowak")
                .put("email", "janusznowak@amg.net"));
        JsonArray participants = new JsonArray();
        participants.add(new JsonObject().put("name", "John")
                .put("surname", "Doe")
                .put("answer", "odp1")
                .put("email", "johndoe@amg.net"));
        winners2.add(new JsonObject().put("name", "Tom")
                .put("surname", "Wróbel")
                .put("email", "tomwrobel@amg.net"));
        JsonArray participants2 = new JsonArray();
        participants2.add(new JsonObject().put("name", "Janusz")
                .put("surname", "Nowak")
                .put("answer", "odp4")
                .put("email", "janusznowak@amg.net"));

        draw1.put("winners", winners)
                .put("incorrectAnswers", "odp1; odp3; odp4")
                .put("lotteryDate", "2016-04-14 10:00:00")
                .put("correctAnswer", "odp2")
                .put("participants", participants);
        draw2.put("winners", winners2)
                .put("incorrectAnswers", "odp1; odp2; odp3")
                .put("lotteryDate", "2016-05-10 10:00:00")
                .put("correctAnswer", "odp4")
                .put("participants", participants2);
        draws.add(draw1)
                .add(draw2);

        mockStatistic.put("draws", draws);

        message.reply(mockStatistic);
    }

    private void loadLotteryDates(Message<JsonObject> message) {
        JsonArray dates = new JsonArray();
        JsonObject mockDates = new JsonObject();

        DateTime date1 = new DateTime(2016, 4, 14, 10, 0);
        DateTime date2 = new DateTime(2016, 4, 21, 10, 0);
        DateTime date3 = new DateTime(2016, 5, 10, 10, 0);
        dates.add(new JsonObject().put("lotteryDate", date1.toString()))
                .add(new JsonObject().put("lotteryDate", date2.toString()))
                .add(new JsonObject().put("lotteryDate", date3.toString()));

        mockDates.put("lotteryDates", dates);
        message.reply(mockDates);
    }

    private void loadLotteryData(Message<JsonObject> message) {
        String incorrectAnswers = "odp1; odp3";
        String correctAnswer = "odp2";
        JsonArray winners = new JsonArray();
        winners.add(new JsonObject().put("name", "Reinke Andreas")
                .put("email", "adres@mail.com"))
                .add(new JsonObject().put("name", "Kadlec Miroslav")
                        .put("email", "adres@mail.com"));
        JsonArray participants = new JsonArray();
        participants.add(new JsonObject().put("name", "Hristov Marian")
                .put("email", "adres@mail.com")
                .put("answer", "odp1"))
                .add(new JsonObject().put("name", "Ratinho E. Rodrigues")
                        .put("email", "adres@mail.com")
                        .put("answer", "odp2"))
                .add(new JsonObject().put("name", "Reich Marco")
                        .put("email", "adres@mail.com")
                        .put("answer", "odp3"))
                .add(new JsonObject().put("name", "Kadlec Miroslav")
                        .put("email", "adres@mail.com")
                        .put("answer", "odp2"))
                .add(new JsonObject().put("name", "Reinke Andreas")
                        .put("email", "adres@mail.com")
                        .put("answer", "odp2"));

        JsonObject mockStatistic = new JsonObject()
                .put("lotteryDate", message.body().getString("lotteryDate"))
                .put("incorrectAnswers", incorrectAnswers)
                .put("correctAnswer", correctAnswer)
                .put("winners", winners)
                .put("participants", participants);

        JsonArray object = new JsonArray();
        object.add(mockStatistic);
        message.reply(new JsonObject().put("lotteryLogs", object));
    }
}
