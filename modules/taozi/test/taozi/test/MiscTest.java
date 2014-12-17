package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.TravelNoteCtrl;
import org.apache.solr.client.solrj.SolrRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.Request;
import org.openqa.selenium.remote.Response;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import controllers.taozi.MiscCtrl;
import org.junit.*;
import play.test.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.GET;

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

    @Test
    public void test() throws Exception {
//        Method method = MiscCtrl.class.getDeclaredMethod("testForTest");
//        method.setAccessible(true);
//
//        MockRequest req = new MockRequest();
//        req.setHeader("UserId", "f123");
//        Result response = (Result) req.apply(method, MiscCtrl.class);
        MiscCtrl.testForTest();
    }

    /**
     * 针对 获得资源上传凭证
     */
    //@Test
    public void testPutPolicy() throws Exception {

    }

}
