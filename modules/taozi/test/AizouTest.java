import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Ignore;
import play.test.WithApplication;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/9/14.
 */
public abstract class AizouTest {

//    protected void assertText(JsonNode node, String field, boolean allowEmpty) {
//        assertText(node, new String[]{field}, allowEmpty);
//    }
//
//    protected void assertText(JsonNode node, String[] fields, boolean allowEmpty) {
//        for (String key : fields) {
//            JsonNode txtNode = node.get(key);
//            if (txtNode == null)
//                assertThat(false).isTrue();
//            else {
//                assertThat(txtNode.isTextual()).isTrue();
//                if (!allowEmpty)
//                    assertThat(txtNode.asText().trim().isEmpty()).isFalse();
//            }
//        }
//    }
//
//    public void assertCoords(double lng, double lat) {
//        assertThat(Math.abs(lng)).isGreaterThan(0);
//        assertThat(Math.abs(lng)).isLessThan(180);
//        assertThat(Math.abs(lat)).isGreaterThan(0);
//        assertThat(Math.abs(lat)).isLessThan(90);
//    }
}
