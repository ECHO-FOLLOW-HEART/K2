package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.Test;
import play.api.mvc.HandlerRef;
import play.libs.Json;

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
    public void planDetailsCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
//                http://api.lvxingpai.com/plans/templates/547bfee9b8ce043fbeaf7391?fromLoc=5473ccd7b8ce043a64108c46&platform=iOS%208.1.2&seq=e560883d7a80cf7825d94d8d503dbc09ae40f6e5&sign=0451a360290c6ac0e896f3de86baa4043a890580&timestamp=1419330555&traffic=1&uid=547c6228e4b0c9f0d576a619&v=1.3.0
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

                JsonNode details = results.get("details");
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
        });
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
