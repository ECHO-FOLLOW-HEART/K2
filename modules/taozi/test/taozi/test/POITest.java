package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.POICtrl;
import controllers.taozi.UserCtrl;
import org.junit.Test;
import play.test.WithApplication;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/8/14.
 */
public class POITest extends WithApplication {

    private void assertText(JsonNode node, String field, boolean allowEmpty) {
        assertText(node, new String[]{field}, allowEmpty);
    }

    private void assertText(JsonNode node, String[] fields, boolean allowEmpty) {
        System.out.println(node.toString());
        for (String key : fields) {
            System.out.println(key);
            JsonNode txtNode = node.get(key);
            if (txtNode == null)
                assertThat(false).isTrue();
            else {
                assertThat(txtNode.isTextual()).isTrue();
                if (!allowEmpty)
                    assertThat(txtNode.asText().trim().isEmpty()).isFalse();
            }
        }
    }

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
}
