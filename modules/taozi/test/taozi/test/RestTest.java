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
    @Test
    public void testSearch() throws Exception {
        Method method = MiscCtrl.class.getDeclaredMethod("searchImpl",
                String.class, String.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, int.class, int.class);
        method.setAccessible(true);
        JsonNode node = (JsonNode) method.invoke(MiscCtrl.class,
                "北", "5473ccd7b8ce043a64108c46", true, false, true, true, true, 0, 3);
        assertThat(node.get("loc")!=null).isTrue();
        assertThat(node.get("loc").size()).isLessThanOrEqualTo(3);
        assertThat(node.get("vs") == null).isTrue();
        assertThat(node.get("hotel")!=null).isTrue();
        assertThat(node.get("restaurant")!=null).isTrue();
        assertThat(node.get("shopping")!=null).isTrue();
    }

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
