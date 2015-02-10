import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.routes;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelpers.*;

/**
 * Created by zephyre on 12/8/14.
 */
@Ignore
public class POITest extends AizouTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("./conf/application.conf"));
//        Configuration config = new Configuration(c);
        app = fakeApplication(new GlobalSettings());
    }

    /**
     * 测试查看某个地点周围的POI的功能
     */
    @Test
    public void getNear() {
        running(app, new PoiNearCheck());
    }

    public class PoiNearCheck implements Runnable {

        @Override
        public void run() {
            HandlerRef<?> handler = routes.ref.POICtrl.getPoiNear(119.228, 39.8, 2000, true, true, false, false, 0, 10, 0, 10);
            Result result = callAction(handler);
            JsonNode node = Json.parse(contentAsString(result));
            assertThat(node.get("code").asInt()).isEqualTo(0);
            JsonNode response = node.get("result");
            for (String poiType : new String[]{"vs", "hotel"}) {
                JsonNode tmp = response.get(poiType);
                assertThat(tmp.isArray()).isTrue();
                assertThat(tmp.size()).isGreaterThan(0);
                for (JsonNode poiNode : tmp) {
                    assertText(poiNode, false, new String[]{"id", "zhName"});
                    assertText(poiNode, true, "desc");
                    assertThat(poiNode.get("images").isArray()).isTrue();
                    JsonNode coords = poiNode.get("location").get("coordinates");
                    double lng = coords.get(0).asDouble();
                    double lat = coords.get(1).asDouble();
                    assertCoords(lng, lat);
                }
            }
        }
    }

    /*@Test
    public void poiNearByCheck() throws ReflectiveOperationException {
//        (double lng, double lat, double maxDist, boolean spot, boolean hotel,
//        boolean restaurant,boolean shopping, int page, int pageSize, int commentPage, int commentPageSize) {
        Method method = POICtrl.class.getDeclaredMethod("getPoiNearImpl", double.class, double.class, double.class,
                boolean.class, boolean.class, boolean.class, boolean.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, 119.228, 39.8, 2000, true, true, false, false, 0, 10, 0, 10);

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
    }*/

    /**
     * 测试通过id获得poi详情
     */
    //TODO 餐厅数据
    @Test
    public void getPoiById() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef handler;
                Map<String, String> map = new HashMap<String, String>();
                map.put("vs", "54814af98b5f77f8306decf4");
                map.put("hotel", "53b053c110114e050b1d24ab");
                map.put("restaurant", "5496a0dcba883386987ce01f");
                for (String type : map.keySet()) {
                    handler = routes.ref.POICtrl.viewPOIInfo(type, map.get(type), 0, 10, 0, 10);
                    Result result = callAction(handler);
                    JsonNode response = Json.parse(contentAsString(result));
                    assertThat(response.get("code").asInt()).isEqualTo(0);
                    response = response.get("result");
                    assertText(response, false, new String[]{"id", "zhName"});
                    assertText(response, true, new String[]{"enName", "priceDesc", "desc", "address", "telephone"});
                    JsonNode coords = response.get("location").get("coordinates");
                    double lng = coords.get(0).asDouble();
                    double lat = coords.get(1).asDouble();
                    assertCoords(lng, lat);
                    for (String field : new String[]{"images", "recommends", "comments"})
                        assertThat(response.get(field).isArray()).isTrue();
                    if (type.equals("vs")) {
                        assertText(response, true, new String[]{"travelMonth", "openTime", "timeCostDesc", "trafficInfoUrl",
                                "kengdieUrl", "guideUrl"});
                    }


                }

            }
        });
    }

    /*@Test
    public void poiInfoCheck() throws ReflectiveOperationException {
        Method method = POICtrl.class.getDeclaredMethod("viewPOIInfoImpl", Class.class, String.class, int.class,
                int.class, Long.class, int.class, int.class);
        method.setAccessible(true);

        Map<String, Class<? extends AbstractPOI>> checker = new HashMap<>();
        checker.put("54814af98b5f77f8306decf4", ViewSpot.class);
        checker.put("53b053c110114e050b1d24ea", Hotel.class);

        for (Map.Entry<String, Class<? extends AbstractPOI>> entry : checker.entrySet()) {
            String oid = entry.getKey();
            Class<? extends AbstractPOI> poiClass = entry.getValue();

            JsonNode ret = (JsonNode) method.invoke(POICtrl.class, poiClass, oid, 0, 10, null, 0, 10);
            assertText(ret, new String[]{"id", "zhName"}, false);
            assertText(ret, new String[]{"enName", "priceDesc", "desc", "address", "telephone"}, true);

            if (poiClass == ViewSpot.class)
                assertText(ret, new String[]{"travelMonth", "openTime", "timeCostDesc", "trafficInfoUrl",
                        "kengdieUrl", "guideUrl"}, true);

            JsonNode coords = ret.get("location").get("coordinates");
            double lng = coords.get(0).asDouble();
            double lat = coords.get(1).asDouble();
            assertCoords(lng, lat);

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
    }*/

    /**
     * 测试评论
     *
     * @throws ReflectiveOperationException
     *//*
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
    }*/


    /**
     * 测试根据目的地获得景点、酒店、餐厅信息
     */
    @Test
    public void getPoiListByLocId() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c46";
                List<String> typeList = new ArrayList();
                typeList.add("vs");
                typeList.add("hotel");
                for (String type : typeList) {
                    HandlerRef<?> handler = routes.ref.POICtrl.viewPoiList(type, locId, "", "rating", "desc", 0, 10, 0, 10);
                    Result result = callAction(handler);
                    JsonNode node = Json.parse(contentAsString(result));
                    assertThat(node.get("code").asInt()).isEqualTo(0);
                    JsonNode response = node.get("result");
                    for (JsonNode tmp : response) {
                        assertText(tmp, false, new String[]{"type", "id", "zhName"});
                        assertThat(tmp.get("rating").asDouble()).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(1);
                        assertThat(tmp.get("images").isArray()).isTrue();
                        JsonNode coords = tmp.get("location").get("coordinates");
                        double lng = coords.get(0).asDouble();
                        double lat = coords.get(1).asDouble();
                        assertCoords(lng, lat);
                    }
                }
            }
        });
    }
}
