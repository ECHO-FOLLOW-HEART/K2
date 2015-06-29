//import com.fasterxml.jackson.databind.JsonNode;
//import com.typesafe.config.Config;
//import com.typesafe.config.ConfigFactory;
//import controllers.app.routes;
//import exception.ErrorCode;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import play.Configuration;
//import play.api.mvc.HandlerRef;
//import play.libs.Json;
//import play.mvc.Result;
//import play.test.FakeApplication;
//import play.test.FakeRequest;
//import utils.validator.DoubleValidator;
//import utils.validator.IntegerValidator;
//import utils.validator.PositiveValidator;
//import utils.validator.RangeValidator;
//
//import java.io.File;
//import java.util.Objects;
//
//import static org.fest.assertions.Assertions.assertThat;
//import static play.test.Helpers.*;
//import static utils.TestHelpers.*;
//
///**
// * Created by zephyre on 2/10/15.
// */
//public class GuideTest {
//    private static FakeApplication app;
//
//    private static Long selfId = 100027L;
//
//    @BeforeClass
//    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
//        Configuration config = new Configuration(c);
//        app = fakeApplication(config.asMap());
//    }
//
//    /**
//     * 获得行程计划列表
//     */
//    @Test
//    public void testGetGuideList() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> handler = routes.ref.GuideCtrl.getGuidesByUser(0, 10);
//                FakeRequest req = fakeRequest(routes.GuideCtrl.getGuidesByUser(0, 10));
//                req.withHeader("UserId", selfId.toString());
//                JsonNode node = getResultNode(handler, req);
//
//                assertThat(node.isArray() && node.size() > 0).isTrue();
//                for (JsonNode guide : node) {
//                    assertFields(guide, "id", "images", "title", "updateTime", "dayCnt", "summary");
//                    assertText(guide, false, "id", "title", "summary");
//                    assertImages(guide.get("images"), true);
//
//                    assertNumber(guide.get("dayCnt"), false, new PositiveValidator(new IntegerValidator(), false));
//                    assertNumber(guide.get("updateTime"), false,
//                            new RangeValidator(new IntegerValidator(), 1e12, null, null));
//
//                    for (String key : new String[]{"dayCnt", "updateTime"}) {
//                        JsonNode val = guide.get(key);
//                        assertThat(val.isIntegralNumber() && val.asLong() >= 0).isTrue();
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 获得行程计划详情
//     */
//    @Test
//    public void testGuideDetails() {
//        running(app, new Runnable() {
//
//            private void checkPoi(JsonNode poi) {
//                assertFields(poi, "id", "zhName", "enName", "rating", "address", "images", "type", "timeCostDesc",
//                        "priceDesc", "rank", "targets", "locality", "location");
//                assertText(poi, false, "id", "zhName", "type");
//                assertText(poi, true, "enName", "address", "timeCostDesc", "priceDesc");
//
//                assertImages(poi.get("images"), true);
//                JsonNode targets = poi.get("targets");
//                assertThat(targets.isArray() && targets.size() > 0).isTrue();
//                for (JsonNode t : targets)
//                    assertThat(t.isTextual() && !t.asText().trim().isEmpty()).isTrue();
//                assertCoords(poi.get("location"));
//
//                assertNumber(poi.get("rating"), false,
//                        new RangeValidator(new DoubleValidator(), 0.0, 1.0, null));
//                assertNumber(poi.get("rank"), true,
//                        new PositiveValidator(new IntegerValidator(), true));
//
//                JsonNode locality = poi.get("locality");
//                if (!locality.isNull()) {
//                    assertText(locality, false, "id", "zhName");
//                    assertText(locality, true, "enName");
//                }
//            }
//
//            private void checkIter(JsonNode item) {
//                if (!item.isArray())
//                    System.out.println(item);
//                assertThat(item.isArray()).isTrue();
//
//                for (JsonNode entry : item) {
//                    assertFields(entry, "dayIndex", "poi");
//                    assertNumber(entry.get("dayIndex"), false,
//                            new PositiveValidator(new IntegerValidator(), false));
//                    checkPoi(entry.get("poi"));
//                }
//            }
//
//            @Override
//            public void run() {
//                String guideId = "54d361f11bf8194dbdcb9fbc";
//                for (String part : new String[]{"all", "itinerary", "shopping", "restaurant"}) {
//                    HandlerRef<?> handler = routes.ref.GuideCtrl.getGuideInfo(guideId, part);
//                    JsonNode node = getResultNode(handler);
//
//                    assertFields(node, "id", "images", "title", "userId", "itineraryDays", "updateTime", "localities",
//                            "itinerary", "shopping", "restaurant", "detailUrl");
//                    assertText(node, false, "id", "title", "detailUrl");
//                    assertImages(node.get("images"), true);
//
//                    assertNumber(node.get("userId"), false, new PositiveValidator(new IntegerValidator(), true));
//                    assertNumber(node.get("updateTime"), false,
//                            new RangeValidator(new IntegerValidator(), 1e12, null, null));
//                    assertNumber(node.get("itineraryDays"), true,
//                            new PositiveValidator(new IntegerValidator(), false));
//
//                    JsonNode locList = node.get("localities");
//                    assertThat(locList.isArray() && locList.size() > 0).isTrue();
//                    for (JsonNode loc : locList) {
//                        assertText(loc, false, "id", "zhName");
//                        assertText(loc, true, "enName");
//                    }
//
//                    switch (part) {
//                        case "all":
//                            checkIter(node.get("itinerary"));
//                        case "itinerary":
//                            checkIter(node.get("itinerary"));
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 修改行程计划
//     */
//    @Test
//    public void testEditGuide() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String guideId = "54d3130b1bf8a9acce393a6d";
//                HandlerRef<?> handler = routes.ref.GuideCtrl.setGuideTitle(guideId);
//                for (Long userId : new Long[]{100000L, selfId}) {
//                    FakeRequest req = fakeRequest(routes.GuideCtrl.setGuideTitle(guideId));
//                    req.withHeader("UserId", userId.toString());
//                    req.withJsonBody(Json.parse("{ \"title\": \"新的一天\" }"));
//
//                    if (Objects.equals(userId, selfId)) {
//                        getResultNode(handler, req);
//                    } else {
//                        Result results = callAction(handler, req);
//                        JsonNode node = Json.parse(contentAsString(results));
//                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.AUTH_ERROR.getVal());
//                    }
//                }
//
//            }
//        });
//    }
//}
