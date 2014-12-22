package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.Test;
import play.api.mvc.HandlerRef;
import play.libs.Json;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/22/14.
 */
public class GeoTest extends TravelPiTest {

    /**
     * 目的地推荐
     */
    @Test
    public void exploreLocCheck() {

        running(app, new Runnable() {
            @Override
            public void run() {
                for (boolean abroad : new boolean[]{true, false}) {
                    HandlerRef<?> handler = routes.ref.MiscCtrl.explore(true, true, false, false, false, abroad, 0, 10);
                    JsonNode results = Json.parse(contentAsString(callAction(handler)));
                    assertThat(results.get("code").asInt()).isEqualTo(0);
                    results = results.get("result");

                    JsonNode locList = results.get("loc");
                    assertThat(locList.isArray()).isTrue();
                    assertThat(locList.size()).isGreaterThan(0);
                    for (JsonNode locNode : locList) {
                        assertText(locNode, new String[]{"_id", "id", "zhName", "name", "fullName"}, false);
                        assertText(locNode, new String[]{"desc", "enName"}, true);

                        assertText(locNode.get("tags"), true);
                        assertText(locNode.get("imageList"), false);
                        assertThat(locNode.get("relVs").isArray()).isTrue();

                        assertThat(Math.abs(locNode.get("lat").asDouble())).isLessThan(90);
                        assertThat(Math.abs(locNode.get("lng").asDouble())).isLessThan(180);

                        double hotness = locNode.get("hotness").asDouble();
                        double rating = locNode.get("rating").asDouble();
                        assertThat(hotness).isGreaterThanOrEqualTo(0);
                        assertThat(hotness).isLessThanOrEqualTo(1);
                        assertThat(rating).isGreaterThanOrEqualTo(0);
                        assertThat(rating).isLessThanOrEqualTo(1);
                        assertThat(locNode.get("ratings").get("ranking").asDouble()).isEqualTo(rating);
                    }
                }
            }
        });
    }

}
