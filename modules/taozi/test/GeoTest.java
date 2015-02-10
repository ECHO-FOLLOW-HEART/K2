import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.taozi.routes;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Configuration;
import play.api.mvc.HandlerRef;
import play.test.FakeApplication;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static utils.TestHelpers.*;

/**
 * Created by zephyre on 12/8/14.
 */
public class GeoTest extends AizouTest {

    private static FakeApplication app;

    private static Long selfId = 100027L;

    @BeforeClass
    public static void setup() {
        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
        Configuration config = new Configuration(c);
        app = fakeApplication(config.asMap());
    }

    /**
     * 国内目的地推荐
     */
    @Test
    public void testDestDomestic() {
        running(app, new Runnable() {
            @Override
            public void run() {
                int page = 0;
                int pageSize = 10;
                HandlerRef<?> handler = routes.ref.GeoCtrl.exploreDestinations(false, page, pageSize);
                JsonNode node = getResultNode(handler);

                assertThat(node.isArray() && node.size() > 0).isTrue();

                for (JsonNode loc : node) {
                    assertFields(loc, "id", "zhName", "enName", "pinyin");
                    assertText(loc, false, "id", "zhName", "pinyin");
                    assertText(loc, true, "enName");
                }
            }
        });
    }

    /**
     * 国外目的地推荐
     */
    @Test
    public void testDestAbroad() {
        running(app, new Runnable() {
            @Override
            public void run() {
                int page = 0;
                int pageSize = 10;
                HandlerRef<?> handler = routes.ref.GeoCtrl.exploreDestinations(true, page, pageSize);
                JsonNode node = getResultNode(handler);

                assertThat(node.isArray() && node.size() > 0).isTrue();

                for (JsonNode loc : node) {
                    assertFields(loc, "id", "images", "zhName", "enName", "code", "desc", "destinations");
                    assertText(loc, false, "id", "zhName", "enName", "code", "enName");
                    assertText(loc, true, "desc");

                    JsonNode images = loc.get("images");
                    assertImages(images, false);

                    JsonNode destList = loc.get("destinations");
                    assertThat(destList.isArray() && destList.size() > 0).isTrue();
                    for (JsonNode dest : destList) {
                        assertFields(dest, "id", "enName", "zhName");
                        assertText(dest, false, "id", "zhName");
                        assertText(dest, true, "enName");
                    }
                }
            }
        });
    }

//    /**
//     * 查看城市详情
//     */
//    @Test
//    public void getLocality() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String locId = "5473ccd7b8ce043a64108c46";
//                int noteId = 0;
//                HandlerRef<?> handler = routes.ref.GeoCtrl.getLocality(locId, noteId);
//                Result result = callAction(handler);
//                JsonNode node = Json.parse(contentAsString(result));
//                assertThat(node.get("code").asInt()).isEqualTo(0);
//                JsonNode response = node.get("result");
//                JsonNode coords = response.get("location").get("coordinates");
//                double lng = coords.get(0).asDouble();
//                double lat = coords.get(1).asDouble();
//                assertCoords(lng, lat);
//                assertText(response, new String[]{"zhName", "desc", "id"}, false);
//                assertText(response, new String[]{"travelMonth", "timeCostDesc"}, true);
//                for (JsonNode tmp : response.get("images"))
//                    assertThat(tmp.get("url").asText()).isNotEmpty();
//            }
//        });
//    }
//
//    /**
//     * 查看图集
//     */
//    @Test
//    public void getAlbums() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String locId = "547aebffb8ce043deccfed0b";
//                int page = 0;
//                int pageSize = 50;
//                HandlerRef<?> handler = routes.ref.MiscCtrl.getAlbums(locId, page, pageSize);
//                Result result = callAction(handler);
//                JsonNode node = Json.parse(contentAsString(result));
//                assertThat(node.get("code").asInt()).isEqualTo(0);
//                JsonNode response = node.get("result");
//                for (JsonNode tmp : response.get("album"))
//                    assertThat(tmp.get("url").asText()).isNotEmpty();
//            }
//        });
//    }
}
