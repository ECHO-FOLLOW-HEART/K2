import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.taozi.routes;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Configuration;
import play.api.mvc.HandlerRef;
import play.test.FakeApplication;
import play.test.FakeRequest;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelpers.*;

/**
 * Created by zephyre on 2/10/15.
 */
public class GuideTest {
    private static FakeApplication app;

    private static Long selfId = 100027L;

    @BeforeClass
    public static void setup() {
        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
        Configuration config = new Configuration(c);
        app = fakeApplication(config.asMap());
    }

    /**
     * 获得行程计划列表
     */
    @Test
    public void testGetGuideList() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.GuideCtrl.getGuidesByUser(0, 10);
                FakeRequest req = fakeRequest(routes.GuideCtrl.getGuidesByUser(0, 10));
                req.withHeader("UserId", selfId.toString());
                JsonNode node = getResultNode(handler, req);

                assertThat(node.isArray() && node.size() > 0).isTrue();
                for (JsonNode guide : node) {
                    assertFields(guide, "id", "images", "title", "updateTime", "dayCnt", "summary");
                    assertText(guide, false, "id", "title", "summary");
                    assertImages(guide.get("images"), true);
                    for (String key : new String[]{"dayCnt", "updateTime"}) {
                        JsonNode val = guide.get(key);
                        assertThat(val.isIntegralNumber() && val.asLong() >= 0).isTrue();
                    }
                }
            }
        });
    }
}
