package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.libs.Json;
import play.mvc.Result;

import controllers.taozi.MiscCtrl;
import org.junit.*;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;


/**
 * Created by Heaven on 2014/12/13.
 */
public class MiscTest extends AizouTest{

    /**
     * 测试获取封面故事功能
     */
    @Test
    public void testCoverStories() throws Exception{
        Method method = MiscCtrl.class.getDeclaredMethod("appHomeImage",
                int.class, int.class, int.class, String.class, int.class);
        method.setAccessible(true);
        int width = 640;
        int height = 1136;
        Result res = (Result) method.invoke(MiscCtrl.class, width, height, 80, "jpg", 1);
        JSONObject response = new JSONObject(contentAsString(res));

        // The response code should be zero
        assertThat(response.getInt("code")).isEqualTo(0);

        // The image's width and height should equal to assigned width and height
        JSONObject result = response.getJSONObject("result");
        assertThat(result.getInt("width")).isEqualTo(width);
        assertThat(result.getInt("height")).isEqualTo(height);

        // The image url should not be null
        assertThat(result.getString("image")).isNotNull();
    }

    /**
     * 测试联合搜索功能
     * @throws Exception
     */
    @Test
    public void testSearch() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("search",
                String.class, String.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, int.class, int.class);
        method.setAccessible(true);
        Result res = (Result) method.invoke(MiscCtrl.class,
                "北", "5473ccd7b8ce043a64108c46", true, false, true, true, true, 0, 3);
        JSONObject result = new JSONObject(contentAsString(res));

        // The return code of result should be 0
        assertThat(result.getInt("code")).isEqualTo(0);

        // The searchResult should have loc, shopping, hotel and restaurant information
        //      should not have vs information
        JSONObject searchResult = result.getJSONObject("result");
        assertThat(searchResult.has("loc")).isTrue();
        assertThat(searchResult.has("shopping")).isTrue();
        assertThat(searchResult.has("hotel")).isTrue();
        assertThat(searchResult.has("restaurant")).isTrue();
        assertThat(searchResult.has("vs")).isFalse();
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
     * @throws Exception
     */
    @Test
    public void testRecommended() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("recommend", int.class, int.class);
        method.setAccessible(true);
        int page = 0;
        int pageSize = 3;
        Result res = (Result) method.invoke(MiscCtrl.class, page, pageSize);
        JSONObject result =new JSONObject(contentAsString(res));

        JSONArray resultList = result.getJSONArray("result");

        // The return code of result should be 0
        assertThat(result.getInt("code")).isEqualTo(0);

        // The size of result should be less or equal than pageSize
        int sizeCount = 0;
        for (int i = 0; i < resultList.length(); i++) {
            JSONArray contentList = resultList.getJSONObject(i).getJSONArray("contents");
            assertThat(contentList.length()).isGreaterThan(0);
            sizeCount += contentList.length();
        }
        assertThat(sizeCount).isLessThanOrEqualTo(pageSize);

        // Each information of each city should be valid
        for (int i = 0; i < resultList.length(); i++) {
            assertThat(resultList.getJSONObject(i).getString("title")).isNotNull();
            JSONArray contents = resultList.getJSONObject(i).getJSONArray("contents");
            for (int j = 0; j < contents.length(); j++) {
                JSONObject city = contents.getJSONObject(j);
                assertThat(city.getString("enName")).isNotNull();
                assertThat(city.getString("zhName")).isNotNull();
                assertThat(city.getString("cover")).isNotNull();
                assertThat(city.getString("desc")).isNotNull();

                // check linkType and linkUrl
                int linkType = city.getInt("linkType");
                if (linkType == 1) {
                    assertThat(city.getString("linkUrl")).isEqualTo("");
                } else if (linkType == 2){
                    assertThat(city.getString("linkUrl")).isNotEqualTo("");
                } else {
                    assertThat(false).isTrue();
                }
            }
        }
    }

//    @Test
//    public void test() throws Exception {
//        Method method = MiscCtrl.class.getDeclaredMethod("testForTest");
//        method.setAccessible(true);
//
//        MockRequest req = new MockRequest();
//        req.setHeader("UserId", "f123");
//        Result response = (Result) req.apply(method, MiscCtrl.class);
//    }

    /**
     * 针对 获得资源上传凭证
     */
    @Test
    public void testPutPolicy() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("putPolicy", String.class);
        method.setAccessible(true);

        MockRequest req = new MockRequest();
        req.setHeader("UserId", "123456");
        Result res = (Result) req.apply(method, MiscCtrl.class, "portrait");
        JsonNode response = Json.parse(contentAsString(res));

        // The response code should be zero
        assertThat(response.get("code").asInt()).isEqualTo(0);

        // The uploadToken and key should not be empty
        JsonNode result = response.get("result");
        assertThat(result.get("uploadToken").asText()).isNotEmpty();
        assertThat(result.get("key").asText()).isNotEmpty();
    }

    /**
     * 针对 上传回调 的测试
     */
    @Test
    public void testGetCallBack() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("getCallback");
        method.setAccessible(true);

        MockRequest req = new MockRequest();
        HashMap<String, String[]> body = new HashMap<>();
        body.put("userId", new String[]{"123456"});
        body.put("url", new String[]{"test.lvxingpai.cn"});
        req.setRequestMap(body);

        Result response = (Result)req.apply(method, MiscCtrl.class);
        JsonNode result = Json.parse(contentAsString(response));

        // The field 'success' of result should be 'true'
        assertThat(result.get("success").asBoolean()).isTrue();

        // The field 'userId' and 'url' should keep unchanged
        assertThat(result.get("userId").asText()).isEqualTo("123456");
        assertThat(result.get("url").asText()).isEqualTo("test.lvxingpai.cn");
    }

    /**
     * 针对 添加收藏 的测试 （返回失败）
     */
    @Test
    public void testAddFavorite_Fail() throws  Exception{
        Method method = MiscCtrl.class.getDeclaredMethod("addFavorite");
        method.setAccessible(true);

        MockRequest req = new MockRequest();
        req.setHeader("UserId", "100084");
        HashMap<String, String> body = new HashMap<>();
        body.put("type", "hotel");
        body.put("itemId", "fake2342");
        req.setRequestJson(body);
        Result res = (Result) req.apply(method, MiscCtrl.class);
        JsonNode response = Json.parse(contentAsString(res));

        // The response code should be 100 because invalid objectId
        assertThat(response.get("code").asInt()).isEqualTo(100);
    }

    /**
     * 针对 添加收藏 的测试 （返回 Favorite item has existed）
     */
    @Test
    public void testAddFavorite_Succ() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("addFavorite");
        method.setAccessible(true);

        MockRequest req = new MockRequest();
        req.setHeader("UserId", "100084");
        HashMap<String, String> body = new HashMap<>();
        body.put("type", "hotel");
        body.put("itemId", "53b053c110114e050b1d24b6");
        req.setRequestJson(body);
        Result res = (Result) req.apply(method, MiscCtrl.class);
        JsonNode response = Json.parse(contentAsString(res));

        // The response code should be 402 because Favorite item has existed
        assertThat(response.get("code").asInt()).isEqualTo(402);

    }

    /**
     * 针对 删除收藏 的测试
     */
    @Test
    public void testDelFavorite() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("delFavorite", String.class);
        method.setAccessible(true);

        String userId = "100032";
        MockRequest req = new MockRequest();
        req.setHeader("UserId", userId);

        Result response = (Result) req.apply(method, MiscCtrl.class, "112233");
        JsonNode result = Json.parse(contentAsString(response));

        // The result code should be 100 because invalid ObjectId
        assertThat(result.get("code").asInt()).isEqualTo(100);
    }

    /**
     * 针对 取得收藏 的测试
     */
    @Test
    public void testGetFavorite() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("getFavorite",
                String.class, int.class, int.class);
        method.setAccessible(true);

        String userId = "100032";
        int page = 0;
        int pageSize = 5;
        MockRequest req = new MockRequest();
        req.setHeader("UserId", userId);

        Result response = (Result) req.apply(method, MiscCtrl.class, "hotel", page, pageSize);
        JsonNode result = Json.parse(contentAsString(response));

        // The response code should be zero
        assertThat(result.get("code").asInt()).isEqualTo(0);

        // The size of FavoriteList should be great than 0 and less or equal than pageSize
        JsonNode favoriteList = result.get("result");
        assertThat(favoriteList.size()).isGreaterThan(0);
        assertThat(favoriteList.size()).isLessThanOrEqualTo(pageSize);
    }
}