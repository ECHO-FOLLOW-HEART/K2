//import com.fasterxml.jackson.databind.JsonNode;
//import com.typesafe.config.Config;
//import com.typesafe.config.ConfigFactory;
//import controllers.app.routes.bac;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import play.Configuration;
//import play.api.mvc.HandlerRef;
//import play.test.FakeApplication;
//import utils.validator.DetailedViewSpotValidator;
//import utils.validator.SimpleRestaurantValidator;
//import utils.validator.SimpleViewSpotValidator;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.fest.assertions.Assertions.assertThat;
//import static play.test.Helpers.fakeApplication;
//import static play.test.Helpers.running;
//import static utils.TestHelpers.*;
//
///**
// * Created by zephyre on 12/8/14.
// */
//public class PoiTest extends AizouTest {
//
//    private static FakeApplication app;
//
//    @BeforeClass
//    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
//        Configuration config = new Configuration(c);
//        app = fakeApplication(config.asMap());
//    }
//
//    /**
//     * 查看景点详情
//     */
//    @Test
//    public void testGetViewSpot() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> handler = routes.ref.POICtrl.viewPOIInfo("vs", "547bfe2fb8ce043eb2d89069", 0, 20,
//                        0, 10, false);
//                JsonNode node = getResultNode(handler);
//
//                new DetailedViewSpotValidator().validate(node);
//            }
//        });
//    }
//
//    /**
//     * 获得HTML格式的景点攻略
//     */
//    @Test
//    public void testViewSpotDetails() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String poiId = "547bfe2fb8ce043eb2d89069";
//                for (String key : new String[]{"tips"}) {
//                    HandlerRef<?> handler = routes.ref.POICtrl.getTravelGuide(poiId, key, "vs");
//                    JsonNode node = getResultNode(handler);
//
//                    assertText(node, true, "desc");
//
//                    JsonNode contents = node.get("contents");
//                    assertThat(contents.isArray() && contents.size() > 0).isTrue();
//                    for (JsonNode c : contents) {
//                        assertFields(c, "title", "desc", "images");
//                        assertText(c, false, "title", "desc");
//                        assertImages(c.get("images"), true);
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 测试查看某个地点周围的POI的功能
//     */
//    @Test
//    public void testNearbyPoi() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> handler = routes.ref.POICtrl.getPoiNear(116, 40, 2000, true, true, true, true,
//                        0, 10, 0, 10);
//                JsonNode node = getResultNode(handler);
//
//                assertFields(node, "vs", "restaurant", "shopping", "hotel");
//                for (String key : new String[]{"vs", "restaurant", "shopping", "hotel"}) {
//                    JsonNode poiList = node.get(key);
//                    assertThat(poiList.isArray());
//                    for (JsonNode poi : poiList) {
//                        switch (key) {
//                            case "vs":
//                                new SimpleViewSpotValidator(Arrays.asList("locality"), null).validate(poi);
//                                break;
//                            case "restaurant":
//                                new SimpleRestaurantValidator(Arrays.asList("locality"),
//                                        Arrays.asList("tel")).validate(poi);
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 查看POI的评论
//     */
//    @Test
//    public void testPoiComments() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String poiId = "54ace71db804666e280f8358";
//                HandlerRef<?> handler = routes.ref.MiscCtrl.displayComment(poiId, 0, 1, 0, 10);
//                JsonNode node = getResultNode(handler);
//                assertThat(node.isArray() && node.size() > 0).isTrue();
//
//                for (JsonNode comment : node) {
//                    assertFields(comment, "id", "images", "userId", "authorAvatar", "authorName", "contents",
//                            "rating", "publishTime");
//                    assertText(comment, false, "id", "contents");
//                    assertText(comment, true, "authorAvatar", "authorName");
//                    assertImages(comment.get("images"), true);
//
//                    JsonNode userId = comment.get("userId");
//                    if (!userId.isNull())
//                        assertThat(userId.isLong() && userId.asLong() > 0).isTrue();
//
//                    JsonNode rating = comment.get("rating");
//                    assertThat(rating.isDouble());
//                    double ratingVal = rating.asDouble();
//                    assertThat(ratingVal >= 0 && ratingVal <= 1).isTrue();
//
//                    JsonNode cTime = comment.get("publishTime");
//                    assertThat(cTime.isLong()).isTrue();
//                    long cTimeVal = cTime.asLong();
//                    assertThat(cTimeVal > 1403572860000L);
//                }
//            }
//        });
//    }
//
//    /**
//     * 测试根据目的地获得景点、酒店、餐厅信息
//     */
//    @Test
//    public void testPoiByLoc() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String locId = "5473ccd7b8ce043a64108c4d";
//                List<String> typeList = new ArrayList<>();
//                typeList.add("vs");
//                typeList.add("restaurant");
//                for (String type : typeList) {
//                    HandlerRef<?> handler = routes.ref.POICtrl.viewPoiList(type, locId, "", "rating", "desc",
//                            0, 10, 0, 10);
//                    JsonNode node = getResultNode(handler);
//                    assertThat(node.isArray() && node.size() > 0);
//
//                    for (JsonNode poi : node) {
//                        switch (type) {
//                            case "vs":
//                                new SimpleViewSpotValidator(Arrays.asList("locality"), null).validate(poi);
//                                break;
//                            case "restaurant":
//                                new SimpleRestaurantValidator(Arrays.asList("locality"), Arrays.asList("tel"))
//                                        .validate(poi);
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//            }
//        });
//    }
//}
