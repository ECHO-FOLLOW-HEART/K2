package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.BeforeClass;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/21/14.
 */
public class MiscTest extends TravelPiTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
        app = fakeApplication(new GlobalSettings());
    }

    /**
     * 获取首页图像
     */
    @Test
    public void homeImageCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                int width = 800;
                int height = 600;
                HandlerRef<?> handler = routes.ref.MiscCtrl.appHomeImage(width, height, 85, null, 1);
                Result result = callAction(handler);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                String imageUrl = node.get("result").get("image").asText();
                assertThat(Pattern.compile("http://lxp-assets\\.qiniudn\\.com/app/").matcher(imageUrl).find()).isTrue();
            }
        });
    }
}
