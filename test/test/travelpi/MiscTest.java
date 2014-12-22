package test.travelpi;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import org.junit.Test;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/21/14.
 */
public class MiscTest extends TravelPiTest {

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

    /**
     * 用户反馈
     */
    @Test
    public void feedbackCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String uid = "53e0a2a80cf2ed3b7eb71057";
                FakeRequest req = fakeRequest(routes.MiscCtrl.postFeedback());
                req.withJsonBody(Json.parse(String.format("{\"uid\":\"%s\", \"body\":\"Test\"}", uid)));
                HandlerRef<?> handler = routes.ref.MiscCtrl.postFeedback();
                JsonNode results = Json.parse(contentAsString(callAction(handler, req)));
                assertThat(results.get("code").asInt()).isEqualTo(0);
            }
        });
    }

    /**
     * 获得输入提示
     */
    @Test
    public void suggestionCheck() {
        running(app, new Runnable() {
            public void checkEntry(JsonNode node) {
                assertText(node, new String[]{"id", "_id", "name", "zhName"}, false);
                assertText(node, new String[]{"enName", "fullName"}, true);
            }

            @Override
            public void run() {
                String word = "上海";
                HandlerRef<?> handler = routes.ref.MiscCtrl.getSuggestions(word, true, true, false, false, 10);
                Result result = callAction(handler);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                node = node.get("result");

                JsonNode locListNode = node.get("loc");
                assertThat(locListNode.isArray());
                assertThat(locListNode.size()).isGreaterThanOrEqualTo(1);
                for (JsonNode locNode : locListNode) {
                    checkEntry(locNode);
                    JsonNode parent = locNode.get("parent");
                    checkEntry(parent);
                }

                JsonNode vsListNode = node.get("vs");
                for (JsonNode vsNode : vsListNode) {
                    assertText(vsNode, new String[]{"_id", "name", "zhName"}, false);
                    assertText(vsNode, "timeCost", true);
                    assertThat(vsNode.get("imageList").isArray()).isTrue();

                    JsonNode ratings = vsNode.get("ratings");
                    for (String key : new String[]{"viewCnt", "favorCnt", "ranking", "checkinCnt"})
                        assertThat(ratings.get(key).asDouble()).isGreaterThanOrEqualTo(0);

                    JsonNode flags = vsNode.get("descriptionFlag");
                    for (String key : new String[]{"desc", "traffic", "details", "tips"})
                        assertThat(flags.get(key).asDouble()).isGreaterThanOrEqualTo(0);
                }
            }
        });
    }
}
