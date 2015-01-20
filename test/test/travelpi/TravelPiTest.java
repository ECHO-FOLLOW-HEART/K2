package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.BeforeClass;
import play.GlobalSettings;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;

/**
 * Created by zephyre on 12/21/14.
 */
public class TravelPiTest {

    protected static FakeApplication app;

    @BeforeClass
    public static void setup() {
        app = fakeApplication(new GlobalSettings());
    }

    protected void assertText(JsonNode node, String field, boolean allowEmpty) {
        assertText(node, new String[]{field}, allowEmpty);
    }

    /**
     * 断言某个节点是字符串列表
     */
    protected void assertText(JsonNode node, boolean allowEmpty) {
        assertThat(node.isArray()).isTrue();
        for (JsonNode theNode : node) {
            assertThat(theNode.isTextual()).isTrue();
            if (!allowEmpty)
                assertThat(theNode.asText().trim().isEmpty()).isFalse();
        }
    }

    protected void assertText(JsonNode node, String[] fields, boolean allowEmpty) {
        for (String key : fields) {
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
}
