package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.routes;
import org.junit.Test;
import play.libs.Json;
import play.mvc.HandlerRef;
import play.mvc.Result;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by Heaven on 2014/12/16.
 */
public class TravelNoteTest extends AizouTest {
    /**
     * 针对 游记搜索 的测试
     */
    @Test
    public void testSearchNotes() throws Exception {
        running(app, new Runnable() {
            @Override
            public void run() {
                int page = 0;
                int pageSize = 3;
                HandlerRef handlerRef = routes.ref.TravelNoteCtrl.searchNotes("北京", "", page, pageSize);
                Result res = callAction(handlerRef);
                JsonNode response = Json.parse(contentAsString(res));

                // The response code should be zero
                assertThat(response.get("code").asInt()).isEqualTo(0);
            }
        });

    }
}
