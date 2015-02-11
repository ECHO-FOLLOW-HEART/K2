import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.taozi.MiscCtrl;
import controllers.taozi.routes;
import exception.ErrorCode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import play.Configuration;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.FakeRequest;
import utils.validator.SimpleRestaurantValidator;
import utils.validator.SimpleViewSpotValidator;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelpers.*;


/**
 * Created by Heaven on 2014/12/13.
 */
public class MiscTest extends AizouTest {

    private static FakeApplication app;

    private static Long selfId = 100027L;

    @BeforeClass
    public static void setup() {
        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
        Configuration config = new Configuration(c);
        app = fakeApplication(config.asMap());
    }

    /**
     * 测试获取封面故事功能
     */
    @Test
    public void testCoverStories() {
        running(app, new Runnable() {
            @Override
            public void run() {
                int width = 800;
                int height = 600;
                int quality = 85;
                HandlerRef<?> handler = routes.ref.MiscCtrl.appHomeImage(width, height, quality, "jpg", 1);
                JsonNode node = getResultNode(handler);
                assertFields(node, "image", "width", "height", "fmt", "quality");

                assertText(node, false, new String[]{"image", "fmt"});
                for (String key : new String[]{"width", "height", "quality"})
                    assertThat(node.get(key).asInt()).isPositive();
            }
        });
    }

    /**
     * 测试联合搜索功能
     *
     * @throws Exception
     */
    @Test
    public void testSearch() {
        running(app, new Runnable() {

            private void checkLocality(JsonNode item) {
                assertFields(item, "id", "zhName", "enName", "isFavorite", "desc", "timeCostDesc", "travelMonth",
                        "imageCnt", "images", "location");
                assertText(item, false, new String[]{"id", "zhName", "desc", "timeCostDesc", "travelMonth"});
                assertText(item, true, "enName");

                assertThat(item.get("isFavorite").isBoolean()).isTrue();
                JsonNode imageCnt = item.get("imageCnt");
                if (!imageCnt.isNull())
                    assertThat(imageCnt.asInt()).isPositive();

                JsonNode images = item.get("images");
                assertImages(images, false);

                assertCoords(item.get("location"));
            }

            private void checkPoi(JsonNode item) {
                assertText(item, false, new String[]{"type", "id", "zhName"});
                assertText(item, true, new String[]{"enName", "priceDesc", "desc", "address"});

                assertThat(item.get("isFavorite").isBoolean()).isTrue();

                JsonNode images = item.get("images");
                assertImages(images, true);

                for (String key : new String[]{"price", "rank"}) {
                    JsonNode val = item.get(key);
                    if (val.isNull())
                        continue;
                    assertThat(val.asInt() >= 0);
                }

                double rating = item.get("rating").asDouble();
                assertThat(rating >= 0 && rating <= 1).isTrue();

                assertCoords(item.get("location"));

                assertThat(item.get("tel").isArray()).isTrue();
            }

            private void checkRestaurant(JsonNode item) {
                assertFields(item, "type", "id", "isFavorite", "zhName", "enName", "price", "priceDesc",
                        "desc", "images", "rating", "address", "location", "rank", "tel");
                checkPoi(item);
            }

            private void checkVs(JsonNode item) {
                assertFields(item, "type", "id", "isFavorite", "zhName", "enName", "price", "priceDesc",
                        "desc", "openTime", "images", "rating", "address", "timeCostDesc", "location", "tipsUrl",
                        "visitGuideUrl", "trafficInfoUrl", "rank", "travelMonth", "tel");
                checkPoi(item);
                assertText(item, true, new String[]{"timeCostDesc", "tipsUrl", "visitGuideUrl", "trafficInfoUrl",
                        "travelMonth", "openTime"});
            }

            @Override
            public void run() {
                String keyword = "北京";
                HandlerRef<?> handler = routes.ref.MiscCtrl.search(keyword, "", true, true, false, true, false, 0, 10);
                JsonNode node = getResultNode(handler);

                assertFields(node, "locality", "vs", "restaurant");

                JsonNode locList = node.get("locality");
                assertThat(locList.isArray() && locList.size() > 0).isTrue();
                for (JsonNode loc : locList)
                    checkLocality(loc);

                JsonNode poiList = node.get("vs");
                assertThat(poiList.isArray() && poiList.size() > 0).isTrue();
                for (JsonNode poi : poiList) {
                    new SimpleViewSpotValidator(Arrays.asList("type"), null).validate(poi);
                    assertThat(poi.get("type").asText()).isEqualTo("vs");
                }

                poiList = node.get("restaurant");
                assertThat(poiList.isArray() && poiList.size() > 0).isTrue();
                for (JsonNode poi : poiList) {
                    new SimpleRestaurantValidator(Arrays.asList("type"), null).validate(poi);
                    assertThat(poi.get("type").asText()).isEqualTo("restaurant");
                }
            }
        });
    }

    /**
     * 测试搜索联想功能
     */
    @Test
    public void testSuggestions() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("getSuggestions",
                String.class, boolean.class, boolean.class, boolean.class, boolean.class, int.class);
        method.setAccessible(true);
        int pageSize = 3;
        Result res = (Result) method.invoke(MiscCtrl.class,
                "北", true, false, false, false, pageSize);
        JSONObject result = new JSONObject(contentAsString(res));

        // The return code of result should be 0
        assertThat(result.getInt("code")).isEqualTo(0);

        // The size of result should be less or equal than pageSize
        JSONObject resultList = result.getJSONObject("result");
        JSONArray loc = resultList.getJSONArray("loc");
        assertThat(loc.length()).isLessThanOrEqualTo(pageSize);
        assertThat(loc.length()).isGreaterThan(0);

    }

    /**
     * 测试首页推荐功能
     *
     * @throws Exception
     */
    @Test
    public void testRecommended() throws Exception {
        running(app, new Runnable() {
            @Override
            public void run() {
                int page = 0;
                int pageSize = 5;
                HandlerRef<?> handler = routes.ref.MiscCtrl.recommend(page, pageSize);
                JsonNode node = getResultNode(handler);

                assertThat(node.isArray()).isTrue();
                assertThat(node.size()).isPositive();

                for (JsonNode section : node) {
                    assertFields(section, "title", "contents");
                    assertText(section, false, "title");

                    JsonNode contents = section.get("contents");
                    assertThat(contents.isArray()).isTrue();
                    assertThat(contents.size()).isPositive();

                    for (JsonNode item : contents) {
                        assertFields(item, "itemId", "title", "itemType", "linkType", "linkUrl", "desc", "cover");
                        assertText(item, false, new String[]{"itemId", "title", "itemType", "linkType", "desc", "cover"});
                        assertText(item, true, "linkUrl");
                    }
                }
            }
        });
    }

    /**
     * 针对 获得资源上传凭证
     */
    @Test
    public void testPutPolicy() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.MiscCtrl.putPolicy("portrait");
                FakeRequest req = fakeRequest(routes.MiscCtrl.putPolicy("portrait"));
                req.withHeader("UserId", "100027");
                JsonNode node = getResultNode(handler, req);
                assertText(node, false, new String[]{"uploadToken", "key"});
            }
        });
    }

    /**
     * 获取运营专栏
     */
    @Test
    public void testColumns() {
        running(app, new Runnable() {
            @Override
            public void run() {
                for (String itemType : new String[]{"homepage", "recommend"}) {
                    HandlerRef<?> handler = routes.ref.MiscCtrl.getColumns(itemType, "");
                    JsonNode node = getResultNode(handler);
                    for (JsonNode c : node) {
                        assertFields(c, "id", "title", "cover", "link", "type");
                        assertText(c, false, new String[]{"id", "title", "cover", "link"});
                        assertThat(c.get("type").asText()).isEqualTo(itemType);
                    }
                }
            }
        });
    }

    /**
     * 用户反馈
     */
    @Test
    public void testFeedback() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.MiscCtrl.postFeedback();
                FakeRequest req = fakeRequest(routes.MiscCtrl.postFeedback());
                req.withHeader("UserId", selfId.toString());
                req.withJsonBody(Json.parse("{\"body\": \"Foobar\"}"));
                getResultNode(handler, req);
            }
        });
    }

    /**
     * 搜索游记
     */
    @Test
    public void testTravelNoteSearch() {
        running(app, new Runnable() {

            private void checkResult(JsonNode note) {
                assertFields(note, "id", "title", "authorName", "authorAvatar", "publishTime", "travelTime", "summary",
                        "source", "essence", "images", "detailUrl");
                assertText(note, false, new String[]{"id", "title", "authorName", "summary", "detailUrl"});
                assertText(note, true, new String[]{"source", "authorAvatar"});
                assertThat(note.get("publishTime").asLong()).isPositive();
                JsonNode travelTime = note.get("travelTime");
                if (!travelTime.isNull())
                    assertThat(travelTime.asLong()).isPositive();

                JsonNode images = note.get("images");
                assertThat(images.isArray()).isTrue();
                for (JsonNode img : images) {
                    assertFields(img, "url");
                    assertText(img, false, "url");
                }
            }

            @Override
            public void run() {
                List<HandlerRef<?>> handlerList = new ArrayList<>();
                handlerList.add(routes.ref.TravelNoteCtrl.searchTravelNotes("三亚", "", 0, 10));
                handlerList.add(routes.ref.TravelNoteCtrl.searchTravelNotes("", "5473cce2b8ce043a64108e12", 0, 10));

                for (HandlerRef<?> handler : handlerList) {
                    JsonNode node = getResultNode(handler);
                    assertThat(node.isArray()).isTrue();
                    assertThat(node.size()).isPositive();
                    for (JsonNode n : node)
                        checkResult(n);
                }
            }
        });
    }

    /**
     * 获得游记详情
     */
    @Test
    public void testTravelNote() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.TravelNoteCtrl.travelNoteDetail("54b257ec5a2df6bba9d19b0d");
                JsonNode node = getResultNode(handler);

                assertFields(node, "id", "title", "authorName", "authorAvatar", "summary", "publishTime",
                        "favorCnt", "commentCnt", "essence", "viewCnt", "rating", "lowerCost", "upperCost",
                        "travelTime", "contents", "source", "images");

                // TODO 需要把favorCnt也纳入到检查范围中
                assertText(node, false, new String[]{"title", "authorName", "authorAvatar"});
                for (String key : new String[]{"publishTime", "commentCnt", "viewCnt",
                        "lowerCost", "upperCost", "travelTime"})
                    assertThat(node.get(key).asLong()).isGreaterThanOrEqualTo(0);

                JsonNode contents = node.get("contents");
                assertThat(contents.isArray()).isTrue();
                assertThat(contents.size()).isPositive();
                for (JsonNode c : contents) {
                    assertFields(c, "content", "title");
                    assertText(c, false, new String[]{"content", "title"});
                }

                JsonNode images = node.get("images");
                assertThat(images.isArray()).isTrue();
                assertThat(images.size()).isPositive();
                for (JsonNode img : images) {
                    assertFields(img, "url");
                    assertText(img, false, "url");
                }
            }
        });
    }

    /**
     * 获得用户收藏
     */
    @Test
    public void testGetFavorites() {
        running(app, new Runnable() {
            private void testEntity(JsonNode node, String... fields) {
                assertFields(node, fields);
                assertText(node, false, new String[]{"id", "itemId", "type", "zhName"});
                assertText(node, true, new String[]{"enName", "desc", "timeCostDesc"});
                for (String key : new String[]{"userId", "createTime"})
                    assertThat(node.get(key).asLong()).isPositive();

                JsonNode images = node.get("images");
                assertThat(images.isArray()).isTrue();
                for (JsonNode img : images)
                    assertText(img, false, "url");

                JsonNode locality = node.get("locality");
                if (locality.size() > 0) {
                    assertText(locality, false, new String[]{"id", "zhName"});
                    assertText(locality, true, "enName");
                }
            }

            private void testEntity(JsonNode node) {
                String[] fields = new String[]{"id", "userId", "itemId", "type", "zhName", "enName", "desc", "images",
                        "createTime", "locality", "timeCostDesc"};
                testEntity(node, fields);
            }

            private void testPoi(JsonNode node) {
                String[] fields = new String[]{"id", "userId", "itemId", "type", "zhName", "enName", "desc", "images",
                        "createTime", "locality", "timeCostDesc", "priceDesc", "rating", "address", "telephone"};
                testEntity(node, fields);
                assertText(node, true, new String[]{"priceDesc", "address", "telephone"});

                JsonNode rating = node.get("rating");
                assertThat(!rating.isNull()).isTrue();
                double ratingVal = rating.asDouble();
                assertThat(ratingVal >= 0 && ratingVal <= 1).isTrue();
            }

            private void testLocality(JsonNode node) {
                testEntity(node);
            }

            private void testViewSpot(JsonNode node) {
                testEntity(node);
            }

            private void testRestaurant(JsonNode node) {
                testPoi(node);
            }

            private void testShopping(JsonNode node) {
                testPoi(node);
            }

            private void testTravelNote(JsonNode node) {
                assertFields(node, "id", "itemId", "type", "userId", "zhName", "enName", "desc", "images", "createTime",
                        "locality");
                assertText(node, false, new String[]{"id", "itemId", "type", "zhName"});
                assertText(node, true, new String[]{"enName", "desc"});
                for (String key : new String[]{"userId", "createTime"})
                    assertThat(node.get(key).asLong()).isPositive();

                JsonNode images = node.get("images");
                assertThat(images.isArray()).isTrue();
                for (JsonNode img : images)
                    assertText(img, false, "url");

                JsonNode locality = node.get("locality");
                if (locality.size() > 0) {
                    assertText(locality, false, new String[]{"id", "zhName"});
                    assertText(locality, true, "enName");
                }
            }

            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.MiscCtrl.getFavorite("", 0, 10);
                FakeRequest req = fakeRequest(routes.MiscCtrl.getFavorite("", 0, 10));
                Long userId = 100009L;
                req.withHeader("UserId", userId.toString());
                JsonNode node = getResultNode(handler, req);

                assertThat(node.isArray() && node.size() > 0).isTrue();

                for (JsonNode item : node) {
                    String itemType = item.get("type").asText();
                    switch (itemType) {
                        case "locality":
                            testLocality(item);
                            break;
                        case "vs":
                            testViewSpot(item);
                            break;
                        case "travelNote":
                            testTravelNote(item);
                            break;
                        case "restaurant":
                            testRestaurant(item);
                            break;
                        default:
                            System.out.println(itemType);
                            assertThat(false).isTrue();
                    }
                }
            }
        });
    }

    @Test
    public void testAddFavorites() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.MiscCtrl.addFavorite();
                FakeRequest req = fakeRequest(routes.MiscCtrl.addFavorite());
                req.withHeader("UserId", selfId.toString());
                req.withJsonBody(Json.parse("{ \"itemId\" :\"547bfdedb8ce043eb2d85033\", \"type\":\"vs\" }"));

                Result result = callAction(handler, req);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isIn(0, ErrorCode.DATA_EXIST);
            }
        });
    }

    @Test
    public void testDelFavorites() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String oid = "547bfdedb8ce043eb2d85033";
                HandlerRef<?> handler = routes.ref.MiscCtrl.delFavorite(oid);
                FakeRequest req = fakeRequest(routes.MiscCtrl.delFavorite(oid));
                req.withHeader("UserId", selfId.toString());

                getResultNode(handler, req);
            }
        });
    }

    /**
     * 针对 上传回调 的测试
     */
    @Test
    @Ignore
    public void testGetCallBack() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("getCallback");
        method.setAccessible(true);

        MockRequest req = new MockRequest();
        HashMap<String, String[]> body = new HashMap<>();
        body.put("userId", new String[]{"123456"});
        body.put("url", new String[]{"test.lvxingpai.cn"});
        req.setRequestMap(body);

        Result response = (Result) req.apply(method, MiscCtrl.class);
        JsonNode result = Json.parse(contentAsString(response));

        // The field 'success' of result should be 'true'
        assertThat(result.get("success").asBoolean()).isTrue();

        // The field 'userId' and 'url' should keep unchanged
        assertThat(result.get("userId").asText()).isEqualTo("123456");
        assertThat(result.get("url").asText()).isEqualTo("test.lvxingpai.cn");
    }
}
