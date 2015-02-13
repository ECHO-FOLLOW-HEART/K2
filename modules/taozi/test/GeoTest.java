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

    /**
     * 查看城市详情
     */
    @Test
    public void testGetLocality() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c46";
                HandlerRef<?> handler = routes.ref.GeoCtrl.getLocality(locId);
                JsonNode node = getResultNode(handler);

                assertFields(node, "id", "zhName", "enName", "isFavorite", "desc", "timeCostDesc", "travelMonth",
                        "imageCnt", "images", "location", "playGuide");
                assertText(node, false, "id", "zhName", "desc", "timeCostDesc", "travelMonth", "playGuide");
                assertText(node, true, "enName");

                assertThat(node.get("isFavorite").isBoolean()).isTrue();
                assertThat(node.get("imageCnt").asInt()).isPositive();

                assertImages(node.get("images"), false);
                assertCoords(node.get("location"));
            }
        });
    }

    /**
     * 查看图集
     */
    @Test
    public void testLocalityAlbum() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c46";
                int page = 0;
                int pageSize = 50;
                HandlerRef<?> handler = routes.ref.GeoCtrl.getLocalityAlbums(locId, page, pageSize);

                JsonNode node = getResultNode(handler);
                assertFields(node, "album", "albumCnt");

                assertThat(node.get("albumCnt").asInt()).isPositive();

                JsonNode album = node.get("album");
                assertThat(album.isArray() && album.size() > 0).isTrue();
                for (JsonNode image : album)
                    assertText(image, false, "url", "originUrl");
            }
        });
    }

    /**
     * 获得目的地深度介绍的大纲
     */
    @Test
    public void testLocalityOutline() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "547aebffb8ce043deccfed0b";
                HandlerRef<?> handler = routes.ref.GeoCtrl.getTravelGuideOutLine(locId);
                JsonNode node = getResultNode(handler);

                assertThat(node.isArray() && node.size() > 0).isTrue();

                for (JsonNode section : node) {
                    assertFields(section, "title", "fields");
                    assertText(section, false, "title");

                    JsonNode fields = section.get("fields");
                    for (JsonNode f : fields)
                        assertThat(f.asText().trim().isEmpty()).isFalse();
                }
            }
        });
    }

    /**
     * 获得目的地深度介绍
     */
    @Test
    public void testLocalityGuide() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c46";
                for (String key : new String[]{"localTraffic", "remoteTraffic", "activities", "tips", "geoHistory"}) {
                    HandlerRef<?> handler = routes.ref.GeoCtrl.getTravelGuide(locId, key);
                    JsonNode node = getResultNode(handler);

                    assertText(node, true, "desc");

                    JsonNode contents = node.get("contents");
                    assertThat(contents.isArray() && contents.size() > 0).isTrue();
                    for (JsonNode c : contents) {
                        assertFields(c, "title", "desc", "images");
                        assertText(c, false, "title", "desc");
                        assertImages(c.get("images"), true);
                    }
                }
            }
        });
    }
}
