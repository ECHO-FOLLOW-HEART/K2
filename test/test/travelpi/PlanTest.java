package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.Test;
import play.api.mvc.HandlerRef;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/23/14.
 */
public class PlanTest extends TravelPiTest {
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

                    assertThat(plan.get("vsCnt").asInt()).isPositive();
                    assertThat(plan.get("forkedCnt").asInt()).isPositive();
                }
            }
        });
    }
}
