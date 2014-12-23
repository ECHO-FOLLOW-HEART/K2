package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.TravelNoteCtrl;
import controllers.taozi.routes;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by Heaven on 2014/12/16.
 */
public class TravelNoteTest extends AizouTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("./conf/application.conf"));
//        Configuration config = new Configuration(c);
        app = fakeApplication(new GlobalSettings());
    }

    /**
     * 针对 游记搜索 的测试
     */
    @Test
    public void testSearchNotes() throws Exception {
        Method method = TravelNoteCtrl.class.getDeclaredMethod("searchNotes",
                String.class, String.class, int.class, int.class);
        method.setAccessible(true);
        int page = 0;
        int pageSize = 3;
        Result res = (Result) method.invoke(TravelNoteCtrl.class,
                "北京", "", page, pageSize);
        JSONObject response = new JSONObject(contentAsString(res));

        // The response code should be zero
        assertThat(response.getInt("code")).isEqualTo(0);
    }

    /**
     * 测试单篇游记详情
     */
    @Test
    public void getTravelNoteDetail() {
        running(app, new Runnable() {
                    @Override
                    public void run() {
                        String noteId = "542972f2b8ce0435c413b750";
                        HandlerRef<?> handler = routes.ref.TravelNoteCtrl.getTravelNoteDetail(noteId);
                        Result result = callAction(handler);
                        JsonNode node = Json.parse(contentAsString(result));
                        assertThat(node.get("code").asInt()).isEqualTo(0);
                        node = node.get("result");
                        assertText(node.get(0), new String[]{"title", "author", "contents"}, false);
                    }
                }
        );
    }

    /**
     * 更多游记
     */
    @Test
    public void getNotes() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c46";
                String keyWord = null;
                int page = 0;
                int pageSize = 10;
                HandlerRef<?> handler = routes.ref.TravelNoteCtrl.getNotes(locId, keyWord, page, pageSize);
                Result result = callAction(handler);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                node = node.get("result").get(0);
                assertText(node, new String[]{"id", "title", "summary"}, false);
            }
        });
    }

    /**
     * 游玩攻略测试
     */
    @Test
    public void getTravelGuide() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "5473ccd7b8ce043a64108c4d";
                String fields = "tips,localTraffic,remoteTraffic,geoHistory,activities,specials";
                HandlerRef<?> handler = routes.ref.POICtrl.getTravelGuide(locId, fields);
                Result result = callAction(handler);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                node = node.get("result");
                String fieldArray[] = fields.split("\\,");
                for (String field : fieldArray) {
                    assertThat(node.get(field).isArray()).isTrue();
                }
            }
        });
    }

    /**
     * 测试评论
     */
    @Test
    //TODO 未通过
    public void displayComment() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String locId = "54844e4522053f7d57d68e85";
                double minRating = 0.1;
                double maxRating = 0.9;
                long lastUpdate = 0;
                int pageSize = 10;
                HandlerRef<?> handler = routes.ref.MiscCtrl.displayComment(locId, minRating, maxRating, lastUpdate, pageSize);
                Result result = callAction(handler);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                node = node.get("result").get(0);
                assertText(node, new String[]{"userName", "contents", "rating"}, false);
            }
        });
    }

    /**
     * 测试首页
     */
    @Test
    public void getColumns() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handlerRef = routes.ref.MiscCtrl.getColumns();
                Result result = callAction(handlerRef);
                JsonNode node = Json.parse(contentAsString(result));
                node = node.get("result");
                for (JsonNode tmp:node)
                    assertText(tmp, new String[]{"title", "cover", "link"}, false);
            }
        });
    }
}
