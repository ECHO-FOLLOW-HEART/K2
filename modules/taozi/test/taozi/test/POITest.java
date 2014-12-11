package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.MiscCtrl;
import controllers.taozi.POICtrl;
import controllers.taozi.UserCtrl;
import models.poi.AbstractPOI;
import models.poi.Hotel;
import models.poi.ViewSpot;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/8/14.
 */
public class POITest extends AizouTest {

    /**
     * 测试查看某个地点周围的POI的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void poiNearByCheck() throws ReflectiveOperationException {
        Method method = POICtrl.class.getDeclaredMethod("getPoiNearImpl", double.class, double.class, double.class,
                boolean.class, boolean.class, boolean.class, int.class, int.class);
        method.setAccessible(true);

        JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, 119.228, 39.8, 2000, true, true, false, 0, 10);

        for (String poiType : new String[]{"vs", "hotel"}) {
            JsonNode node = ret.get(poiType);
            assertThat(node.size()).isGreaterThan(0);
            for (JsonNode poiNode : node) {
                assertText(poiNode, "zhName", false);
                assertText(poiNode, new String[]{"enName", "desc"}, true);

                JsonNode imagesNode = poiNode.get("images");
                assertThat(imagesNode.size()).isGreaterThan(0);
                for (JsonNode imgEntry : imagesNode) {
                    assertText(imgEntry, "url", false);
                    for (String key : new String[]{"width", "height"})
                        assertThat(imgEntry.get(key).asInt()).isGreaterThan(0);
                }

                JsonNode coords = poiNode.get("location").get("coordinates");
                double lng = coords.get(0).asDouble();
                double lat = coords.get(1).asDouble();
                assertThat(Math.abs(lng)).isGreaterThan(0);
                assertThat(Math.abs(lng)).isLessThan(180);
                assertThat(Math.abs(lat)).isGreaterThan(0);
                assertThat(Math.abs(lat)).isLessThan(90);
            }
        }
    }

    /**
     * 测试获得景点详情
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void poiInfoCheck() throws ReflectiveOperationException {

        Method method = POICtrl.class.getDeclaredMethod("viewPOIInfoImpl", Class.class, String.class, int.class,
                int.class, int.class, int.class);
        method.setAccessible(true);

        Map<String, Class<? extends AbstractPOI>> checker = new HashMap<>();
        checker.put("54814af98b5f77f8306decf4", ViewSpot.class);
        checker.put("53b053c110114e050b1d24ea", Hotel.class);

        for (Map.Entry<String, Class<? extends AbstractPOI>> entry : checker.entrySet()) {
            String oid = entry.getKey();
            Class<? extends AbstractPOI> poiClass = entry.getValue();

            JsonNode ret = (JsonNode) method.invoke(POICtrl.class, poiClass, oid, 0, 10, 0, 10);
            assertText(ret, new String[]{"id", "zhName"}, false);
            assertText(ret, new String[]{"enName", "priceDesc", "desc", "address", "telephone"}, true);

            if (poiClass == ViewSpot.class)
                assertText(ret, new String[]{"travelMonth", "openTime", "timeCostDesc", "trafficInfoUrl",
                        "kengdieUrl", "guideUrl"}, true);

            JsonNode coords = ret.get("location").get("coordinates");
            double lng = coords.get(0).asDouble();
            double lat = coords.get(1).asDouble();
            assertThat(Math.abs(lng)).isGreaterThan(0);
            assertThat(Math.abs(lng)).isLessThan(180);
            assertThat(Math.abs(lat)).isGreaterThan(0);
            assertThat(Math.abs(lat)).isLessThan(90);

            JsonNode imagesNode = ret.get("images");
            assertThat(imagesNode.size()).isGreaterThan(0);
            for (JsonNode imgEntry : imagesNode) {
                assertText(imgEntry, "url", false);
                for (String key : new String[]{"width", "height"})
                    assertThat(imgEntry.get(key).asInt()).isGreaterThan(0);
            }

            for (String key : new String[]{"recommends", "comments"})
                assertThat(ret.get(key).isArray()).isTrue();
        }
    }

    /**
     * 测试评论
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void commentsCheck() throws ReflectiveOperationException {

        Method method = MiscCtrl.class.getDeclaredMethod("getCommentsImpl", String.class, double.class, double.class,
                long.class, int.class);
        method.setAccessible(true);

        double minRating = 0.45;
        double maxRating = 0.8;
        String poiId = "548040a89fb7882b6dca5fa2";
        long lastUpdate = 0;
        JsonNode result = (JsonNode) method.invoke(MiscCtrl.class, poiId, minRating, maxRating, lastUpdate, 100);

        for (JsonNode comment : result) {
            assertText(comment, new String[]{"userAvatar", "userName", "contents"}, true);
            JsonNode imagesNode = comment.get("images");
            assertThat(imagesNode.isArray()).isTrue();
            JsonNode tsNode = comment.get("cTime");
            assertThat(tsNode.asLong()).isGreaterThan(0);
        }
    }
}
