package taozi.test;

import aizou.core.PoiAPI;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.MiscCtrl;
import controllers.taozi.POICtrl;
import controllers.taozi.UserCtrl;
import models.poi.AbstractPOI;
import models.poi.Hotel;
import models.poi.ViewSpot;
import org.junit.Test;
import org.specs2.json.Json;

import javax.xml.transform.Result;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

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
        Method method = MiscCtrl.class.getDeclaredMethod("recommendedImpl", int.class, int.class);
        method.setAccessible(true);
        int page = 0;
        int pageSize = 3;
        JsonNode node = (JsonNode) method.invoke(MiscCtrl.class, page, pageSize);

        // The size of node should be less or equal than pageSize
        int sizeCount = 0;
        for (JsonNode subnode : node) {
            assertThat(subnode.get("contents").size()).isGreaterThan(0);
            sizeCount += subnode.get("contents").size();
        }
        assertThat(sizeCount).isLessThanOrEqualTo(pageSize);

        // The content of each subnode should be valid
        for (JsonNode subnode : node) {
            for (JsonNode city : subnode.get("contents")) {
                assertText(city, new String[]{"cover", "desc", "zhName", "enName"}, false);

                // linkUrl should be NULL for linkType==1, not NULL for linkType==2
                int cityLinkType = city.get("linkType").asInt();
                if (cityLinkType == 1) {
                    assertText(city, "linkUrl", true);
                } else if (cityLinkType == 2) {
                    assertText(city, "linkUrl", false);
                } else {
                    assertThat(false).isTrue();
                }
            }
        }
    }

}
