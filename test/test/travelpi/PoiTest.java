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
public class PoiTest extends TravelPiTest {

    /**
     * 查询某个目的地内的景点
     */
    @Test
    public void poiLocCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.POICtrl.poiSearch("vs", "547dca608b5f77f8305ef1a2", "", "",
                        0, 10, "", "asc", "");
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);

                results = results.get("result");
                assertThat(results.isArray()).isTrue();
                assertThat(results.size()).isGreaterThan(0);

                for (JsonNode vsNode : results) {
                    assertThat(vsNode.get("type").asText()).isEqualTo("vs");
                    assertText(vsNode, new String[]{"id", "_id", "name", "zhName", "fullName"}, false);
                    assertText(vsNode, new String[]{"desc", "timeCostDesc"}, true);
                    assertText(vsNode.get("imageList"), false);

                    assertThat(vsNode.has("price"));
                    assertThat(vsNode.has("timeCost"));

                    JsonNode addr = vsNode.get("addr");
//                    assertText(addr, new String[]{"locId", "locName"}, false);
                    assertText(addr, "addr", true);
                    assertThat(Math.abs(addr.get("lat").asDouble())).isLessThan(90);
                    assertThat(Math.abs(addr.get("lng").asDouble())).isLessThan(180);

                    double rating = vsNode.get("ratings").get("ranking").asDouble();
                    assertThat(rating).isGreaterThan(0);
                    assertThat(rating).isLessThan(1);

                    assertText(vsNode.get("contact").get("phoneList"), true);
                }
            }
        });
    }

    /**
     * 查询景点详情
     */
    @Test
    public void vsInfoCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.POICtrl.viewSpotInfo("vs", "547bfe05b8ce043eb2d86759",
                        true, false, 10);
                JsonNode results = Json.parse(contentAsString(callAction(handler)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
                results = results.get("result");
                assertText(results, new String[]{"_id", "name", "zhName", "desc"}, false);
                assertText(results, new String[]{"priceDesc", "timeCost", "openTime", "trafficInfo", "guide",
                        "kengdie"}, true);
                assertText(results.get("tags"), true);
                assertText(results.get("imageList"), false);

                JsonNode addr = results.get("addr");
                assertText(addr, new String[]{"locId", "locName", "addr"}, true);
                double lat = addr.get("lat").asDouble();
                double lng = addr.get("lng").asDouble();
                assertThat(Math.abs(lat)).isLessThan(90);
                assertThat(Math.abs(lng)).isLessThan(180);

                assertThat(results.get("travelMonth").isArray()).isTrue();

                JsonNode desc = results.get("descriptionFlag");
                for (String key : new String[]{"desc", "traffic", "details", "tips"})
                    assertThat(desc.get(key).asInt()).isIn(0, 1);

                JsonNode ratings = results.get("ratings");
                for (String key : new String[]{"viewCnt", "favorCnt", "checkinCnt"})
                    assertThat(ratings.get(key).asInt()).isPositive();
                double ranking = ratings.get("ranking").asDouble();
                assertThat(ranking).isGreaterThanOrEqualTo(0);
                assertThat(ranking).isLessThanOrEqualTo(1);
            }
        });
    }
}
