import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.routes;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.test.FakeApplication;

import play.mvc.Result;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelpers.*;

/**
 * Created by zephyre on 12/8/14.
 */
@Ignore
public class GeoTest extends AizouTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("./conf/application.conf"));
//        Configuration config = new Configuration(c);
        app = fakeApplication(new GlobalSettings());
    }

    /**
     * 目的地列表测试
     */
    @Test
    public void exploreDestinations() {
        running(app, new Runnable() {
            @Override
            public void run() {
                int page = 0;
                int pageSize = 10;
                for (Boolean abroad : new Boolean[]{true, false}) {
                    HandlerRef<?> handler = routes.ref.GeoCtrl.exploreDestinations(abroad, page, pageSize);
                    Result result = callAction(handler);
                    JsonNode node = Json.parse(contentAsString(result));
                    assertThat(node.get("code").asInt()).isEqualTo(0);
                    JsonNode response = node.get("result");
                    if (abroad.equals(false)) {
                        for (JsonNode tmp : response) {
                            assertText(tmp, new String[]{"id", "zhName"}, false);
                        }
                    } else {
                        for (JsonNode tmp : response) {
                            assertText(tmp, new String[]{"id", "zhName"}, false);
                            JsonNode dest = tmp.get("destinations");
                            for (JsonNode tmpNode : dest) {
                                assertText(tmpNode, new String[]{"id", "zhName"}, false);
                            }
                        }
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
