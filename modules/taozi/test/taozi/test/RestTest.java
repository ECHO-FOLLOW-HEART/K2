package taozi.test;

import org.json.JSONArray;
import org.json.JSONObject;
import  play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.MiscCtrl;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

/**
 * Created by Heaven on 2014/12/13.
 */
public class RestTest extends AizouTest{

    /**
     * 测试获取封面故事功能
     */
//    @Test
//    public void testCoverStories() throws Exception{
//        Method method = MiscCtrl.class.getDeclaredMethod("getAppHomeImageImpl",
//                int.class, int.class, int.class, String.class, int.class);
//        method.setAccessible(true);
//        JsonNode node = (JsonNode) method.invoke(MiscCtrl.class, 640, 1136, 80, "jpg", 1);
//
//        assertText(node, new String[]{"title", "content", "contentType", "image", "fmt"}, true);
//        assertThat(node.get("contentType").equals("html") || node.get("contentType").equals("plain")).isTrue();
//        assertThat(node.get("width").equals(640)).isTrue();
//        assertThat(node.get("height").equals(1136)).isTrue();
//    }

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
}
