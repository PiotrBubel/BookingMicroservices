package pl.lodz.p.microservices.management.booking;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import pl.lodz.p.microservices.management.booking.BookingManagement;
//import pl.lodz.p.microservices.management.users.UsersManagement;

import static junit.framework.TestCase.fail;

@RunWith(VertxUnitRunner.class)
public class ServicesManagementTest {
    private static final String VERTICLE_ADDRESS = "net.atos.pip.davay.bilet.UsersManagement";

    private static final String METHOD = "method";

    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(BookingManagement.class.getName(), context.asyncAssertSuccess());
//        vertx.deployVerticle(DatabaseProxyServiceMock.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) throws Exception {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void shouldGetOneGeneralData(TestContext context) {
        final Async async = context.async();

        DateTime lotteryDate = new DateTime(2016, 03, 15, 10, 0);
        String wrongAnswers = "odp1; odp3";
        String correctAnswer = "odp2";
        Integer numberOfEmployees = 5;
        Double percentOfCorrectAnswers = 300.0/5.0;
        Integer numberOfWinnings = 2;
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

        JsonObject statistic = new JsonObject()
                .put("lotteryDate", lotteryDate.toString())
                .put("incorrectAnswers", wrongAnswers)
                .put("correctAnswer", correctAnswer)
                .put("numberOfEmployees", numberOfEmployees)
                .put("percentOfCorrectAnswers", percentOfCorrectAnswers)
                .put("numberOfWinnings", numberOfWinnings)
                .put("winners", winners)
                .put("participants", participants);

        JsonObject date = new JsonObject().put("lotteryDate", lotteryDate.toString());

        EventBus eventBus = vertx.eventBus();
        eventBus.send(VERTICLE_ADDRESS, date,
                new DeliveryOptions().addHeader(METHOD, "SHOW_LOTTERY_STATISTICS"),
                (AsyncResult<Message<JsonObject>> messageAsyncResult) -> {

                    if (messageAsyncResult.succeeded()) {
                        context.assertEquals(statistic, messageAsyncResult.result().body());
                        async.complete();
                    } else {
                        fail();
                    }
                });
    }

    @Test
    public void shouldGetLotteriesDates(TestContext context) {
        final Async async = context.async();

        EventBus eventBus = vertx.eventBus();
        eventBus.send(VERTICLE_ADDRESS, "hello",
                new DeliveryOptions().addHeader(METHOD, "SHOW_LOTTERY_DATES"),
                (AsyncResult<Message<JsonObject>> messageAsyncResult) -> {

                    JsonArray datesList = messageAsyncResult.result().body().getJsonArray("lotteryDates");
                    if (messageAsyncResult.succeeded()) {
                        context.assertTrue(datesList.size() == 3);
                        async.complete();
                    } else {
                        fail();
                    }
                });
    }

    @Test
    public void shouldGetNumbersFromPersonalStatistics(TestContext context) {
        final Async async = context.async();

        JsonObject object = new JsonObject();
        String userEmail = "janusznowak@amg.net";
        object.put("userEmail", userEmail);

        EventBus eventBus = vertx.eventBus();
        eventBus.send(VERTICLE_ADDRESS, object,
                new DeliveryOptions().addHeader(METHOD, "SHOW_PERSONAL_STATISTICS"),
                (AsyncResult<Message<JsonObject>> messageAsyncResult) -> {

                    if (messageAsyncResult.succeeded()) {
                        Integer numberOfWinnings = 1;
                        Integer numberFromWinnings = messageAsyncResult.result().body().getInteger("numberOfWinnings");
                        Integer numberOfLotteries = 2;
                        Integer numberFromDBLotteries = messageAsyncResult.result().body().getInteger("numberOfLotteries");
                        context.assertEquals(numberOfWinnings, numberFromWinnings);
                        context.assertEquals(numberOfLotteries, numberFromDBLotteries);

                        async.complete();
                    } else {
                        fail();
                    }
                });
    }
}
