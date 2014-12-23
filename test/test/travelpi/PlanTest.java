package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.Test;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.test.FakeRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/23/14.
 */
public class PlanTest extends TravelPiTest {
    /**
     * 获得路线列表
     */
    @Test
    public void planListCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                final String fromLoc = "5473ccd7b8ce043a64108c46";
                final String loc = "547aebffb8ce043deccfed0b";

                HandlerRef<?> handler = routes.ref.PlanCtrl.explorePlans(fromLoc, loc, "", "", "asc", "", 0, 999, 0, 10);
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");
                assertThat(results.isArray()).isTrue();
                assertThat(results.size()).isPositive();

                for (JsonNode plan : results) {
                    planBaseCheck(plan);
                }
            }
        });
    }

    /**
     * 获得路线详情
     */
    @Test
    public void planInfoCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String planId = "547bfee9b8ce043fbeaf7391";
                String fromLoc = "5473ccd7b8ce043a64108c46";
                HandlerRef<?> handler = routes.ref.PlanCtrl.getPlanFromTemplates(planId, fromLoc, "", 0, "");
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");

                planBaseCheck(results);

                assertText(results, "moreDesc", true);
                assertText(results, "templateId", false);
                assertThat(results.get("updateTime").isIntegralNumber()).isTrue();

                planDetailsCheck(results.get("details"));
            }
        });
    }

    /**
     * 优化路线
     */
    @Test
    public void optimizerCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String postDataStr = "{ \"endDate\": \"2014-12-26 00:00:00+0800\", \"templateId\": " +
                        "\"547bfee9b8ce043fbeaf7391\", " +
                        "\"title\": \"优雅的旅程\", \"viewBudget\": 0, \"trafficBudget\": 6574, \"details\": [ { \"st\": " +
                        "\"2014-12-26 00:00:00+0800\", \"type\": \"vs\", \"itemId\": \"547bfe08b8ce043eb2d869fd\" } ], " +
                        "\"_id\": \"549943fc0cf28465d161a75f\", \"optLevel\": 1, \"stayBudget\": 900, \"startDate\": " +
                        "\"2014-12-26 00:00:00+0800\", \"budget\": [ 7500, 18700 ]}";

                JsonNode postData = Json.parse(postDataStr);
                FakeRequest req = fakeRequest("POST", "/plans/optimizers?platform=iOS8.1.2");
                req.withJsonBody(postData);
                HandlerRef<?> handler = routes.ref.PlanCtrl.optimizePlan();
                JsonNode results = Json.parse(contentAsString(callAction(handler, req)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");

                planDetailsCheck(results.get("details"));
            }
        });
    }

    /**
     * 获得UGC路线列表
     */
    @Test
    public void ugcPlanListCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.PlanCtrl.getUGCPlans1("547c6228e4b0c9f0d576a619", "", 0, 20);
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");

                assertThat(results.isArray()).isTrue();
                assertThat(results.size()).isPositive();

                SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                for (JsonNode plan : results) {
                    assertText(plan, new String[]{"_id", "id", "title"}, false);
                    for (String key : new String[]{"startDate", "endDate"}) {
                        try {
                            dateFmt.parse(plan.get(key).asText());
                        } catch (ParseException e) {
                            assertThat(false).isTrue();
                            break;
                        }
                    }
                    assertThat(plan.get("updateTime").asInt()).isPositive();
                    assertText(plan.get("imageList"), false);
                }
            }
        });
    }

    /**
     * 获得UGC路线详情
     */
    @Test
    public void ugcPlanDetailsCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.PlanCtrl.getUGCPlans1("", "549943fc0cf28465d161a75f", 0, 20);
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");

                planBaseCheck(results);
                planDetailsCheck(results.get("details"));

                assertText(results, "moreDesc", true);
                assertText(results, "templateId", false);
                assertThat(results.get("updateTime").isIntegralNumber()).isTrue();
            }
        });
    }

    private void planDetailsCheck(JsonNode details) {
        assertThat(details.isArray()).isTrue();
        assertThat(details.size()).isPositive();
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        for (JsonNode dayEntry : details) {
            String dateString = dayEntry.get("date").asText();
            try {
                dateFmt.parse(dateString);
            } catch (ParseException e) {
                assertThat(false).isTrue();
                break;
            }

            JsonNode actv = dayEntry.get("actv");
            assertThat(actv.isArray()).isTrue();
            assertThat(actv.size()).isGreaterThanOrEqualTo(0);

            for (JsonNode actvEntry : actv) {
                assertText(actvEntry, new String[]{"itemId", "itemName", "locId", "locName", "type"}, false);
                assertText(actvEntry, new String[]{"subType", "ts"}, true);
                assertText(actvEntry, "type", false);
                JsonNode itemDetails = actvEntry.get("details");
                assertThat(itemDetails != null).isTrue();
                if (itemDetails != null)
                    assertThat(itemDetails.isNull()).isFalse();
            }
        }
    }

    private void planBaseCheck(JsonNode plan) {
        assertText(plan, new String[]{"_id", "title"}, false);
        assertText(plan, new String[]{"authorName", "authorAvatar"}, true);
        for (String key : new String[]{"tags", "summary", "imageList"})
            assertText(plan.get(key), false);
        assertText(plan.get("lxpTag"), true);

        List<JsonNode> targetList = new ArrayList<>();
        targetList.add(plan.get("target"));
        for (JsonNode t : plan.get("targets"))
            targetList.add(t);
        for (JsonNode t : targetList) {
            assertText(t, new String[]{"_id", "name"}, false);
            assertText(t, "enName", true);
        }

        assertThat(plan.get("days").asInt()).isPositive();
        assertThat(plan.get("stayBudget").asInt()).isPositive();
        assertThat(plan.get("trafficBudget").asInt()).isPositive();
        assertThat(plan.get("viewBudget").asInt()).isGreaterThanOrEqualTo(0);

        int lowerBudget = plan.get("budget").get(0).asInt();
        int upperBudget = plan.get("budget").get(1).asInt();
        assertThat(lowerBudget).isLessThan(upperBudget);
        assertThat(lowerBudget).isPositive();

        assertThat(plan.get("vsCnt").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(plan.get("forkedCnt").asInt()).isGreaterThanOrEqualTo(0);
    }
}
